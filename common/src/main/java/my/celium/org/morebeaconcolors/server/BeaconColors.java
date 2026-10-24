package my.celium.org.morebeaconcolors.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import my.celium.org.morebeaconcolors.data.BeamStore;
import my.celium.org.morebeaconcolors.data.BeamStyle;
import my.celium.org.morebeaconcolors.net.BeamsPacket;
import my.celium.org.morebeaconcolors.net.Net;
import my.celium.org.morebeaconcolors.net.SetBeamPacket;
import my.celium.org.schedule.MycelScheduler;
import my.celium.org.schedule.ScheduledTask;
import my.celium.org.util.ChatVoice;
import my.celium.org.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;

/**
 * The server half: one {@link BeamStore} per world, saved as
 * {@code <world>/data/morebeaconcolors.json}. Players with the mod get every
 * style when they join and each change as it happens. Every 5 seconds,
 * entries whose beacon is gone (in loaded chunks) are dropped.
 */
public final class BeaconColors {
    static final ChatVoice VOICE = TextUtil.voice("Beacon");
    private static final long SWEEP_TICKS = 100;
    /** How far a player may be from the beacon, like the beacon menu itself. */
    private static final double REACH_SQ = 8 * 8;

    private static final BeamStore STORE = new BeamStore();
    private static MinecraftServer server;
    private static Path file;
    private static ScheduledTask sweep;
    private static Field menuAccess;

    private BeaconColors() {
    }

    public static synchronized void start(MinecraftServer started) {
        server = started;
        file = started.getWorldPath(LevelResource.ROOT).resolve("data").resolve(MoreBeaconColorsMod.ID + ".json");
        try {
            STORE.load(file);
            MoreBeaconColorsMod.debug("Loaded {} colored beacons", STORE.size());
        } catch (IOException e) {
            MoreBeaconColorsMod.warn("{}", e.getMessage());
        }
        sweep = MycelScheduler.runEvery(SWEEP_TICKS, BeaconColors::sweep);
    }

    public static synchronized void stop() {
        if (sweep != null) {
            sweep.cancel();
            sweep = null;
        }
        STORE.clear();
        server = null;
        file = null;
    }

    private static void save() {
        if (file == null) {
            return;
        }
        try {
            STORE.save(file);
        } catch (IOException e) {
            MoreBeaconColorsMod.warn("Could not save {}: {}", file, e.toString());
        }
    }

    public static BeamStore.Key key(Level level, BlockPos pos) {
        return new BeamStore.Key(level.dimension().identifier().toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    /** Sends every style to a player who just joined; an empty set still tells the client the server has the mod. */
    public static void sendAll(ServerPlayer player) {
        List<BeamsPacket.Entry> entries = new ArrayList<>();
        STORE.all().forEach((key, style) -> entries.add(new BeamsPacket.Entry(key, style)));
        boolean first = true;
        int i = 0;
        do {
            List<BeamsPacket.Entry> chunk = entries.subList(i, Math.min(entries.size(), i + BeamsPacket.MAX_ENTRIES));
            Net.send(player, new BeamsPacket(first, new ArrayList<>(chunk)));
            first = false;
            i += BeamsPacket.MAX_ENTRIES;
        } while (i < entries.size());
    }

    private static void broadcast(List<BeamsPacket.Entry> entries) {
        if (server == null || entries.isEmpty()) {
            return;
        }
        BeamsPacket packet = new BeamsPacket(false, entries);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Net.send(player, packet);
        }
    }

    /** A player picked a style in the color menu. */
    public static void onSetBeam(SetBeamPacket packet, ServerPlayer player) {
        if (player == null || server == null) {
            return;
        }
        if (!MoreBeaconColorsMod.isModEnabled()) {
            player.sendSystemMessage(VOICE.fail("Beam colors are turned off on this server."));
            return;
        }
        BeamStyle style = packet.style();
        if (style.animated() && !MoreBeaconColorsMod.allowAnimated.get()) {
            player.sendSystemMessage(VOICE.fail("Animated beams are turned off on this server."));
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = openBeacon(player).orElse(packet.pos());
        if (!level.isLoaded(pos) || !level.getBlockState(pos).is(Blocks.BEACON)
                || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > REACH_SQ) {
            MoreBeaconColorsMod.debug("{} tried to color a beacon at {} they can't reach", player.getName().getString(), pos);
            return;
        }
        BeamStore.Key key = key(level, pos);
        if (STORE.put(key, style)) {
            save();
            broadcast(List.of(new BeamsPacket.Entry(key, style)));
            MoreBeaconColorsMod.debug("{} set {} to {}", player.getName().getString(), key, style);
        }
    }

    /** The beacon behind the player's open beacon menu, read from the menu itself. */
    private static Optional<BlockPos> openBeacon(ServerPlayer player) {
        if (!(player.containerMenu instanceof BeaconMenu menu)) {
            return Optional.empty();
        }
        try {
            if (menuAccess == null) {
                Field field = BeaconMenu.class.getDeclaredField("access");
                field.setAccessible(true);
                menuAccess = field;
            }
            ContainerLevelAccess access = (ContainerLevelAccess) menuAccess.get(menu);
            return access.evaluate((level, pos) -> pos);
        } catch (ReflectiveOperationException | RuntimeException e) {
            MoreBeaconColorsMod.debug("Beacon menu position not readable: {}", e.toString());
            return Optional.empty();
        }
    }

    /** Drops entries whose beacon was broken, in chunks that are loaded right now. */
    private static void sweep() {
        if (server == null || STORE.size() == 0) {
            return;
        }
        List<BeamsPacket.Entry> removed = new ArrayList<>();
        for (Map.Entry<BeamStore.Key, BeamStyle> entry : new ArrayList<>(STORE.all().entrySet())) {
            BeamStore.Key key = entry.getKey();
            ServerLevel level = levelOf(key.dimension());
            if (level == null) {
                continue;
            }
            BlockPos pos = new BlockPos(key.x(), key.y(), key.z());
            if (level.isLoaded(pos) && !level.getBlockState(pos).is(Blocks.BEACON)) {
                STORE.remove(key);
                removed.add(new BeamsPacket.Entry(key, BeamStyle.VANILLA));
            }
        }
        if (!removed.isEmpty()) {
            MoreBeaconColorsMod.debug("Forgot {} broken beacons", removed.size());
            save();
            broadcast(removed);
        }
    }

    private static ServerLevel levelOf(String dimension) {
        Identifier id = Identifier.tryParse(dimension);
        return id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
    }
}
