package my.celium.org.morebeaconcolors.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import my.celium.org.Mycel;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import my.celium.org.morebeaconcolors.data.BeamPainter;
import my.celium.org.morebeaconcolors.data.BeamStore;
import my.celium.org.morebeaconcolors.data.BeamStyle;
import my.celium.org.morebeaconcolors.net.BeamsPacket;
import my.celium.org.morebeaconcolors.net.SetBeamPacket;
import my.celium.org.network.MycelNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The client half. Styles come from the server when it has the mod
 * ({@link #receive}); otherwise from a local store in
 * {@code config/morebeaconcolors-local.json}, keyed by server address, that
 * only this player sees. Each tick, colored beacons near the player get their
 * beam sections swapped for painted ones; vanilla's own list comes back as
 * soon as a beacon goes back to vanilla. Client only.
 */
public final class ClientBeams {
    private static final BeamStore SYNCED = new BeamStore();
    private static final BeamStore LOCAL = new BeamStore();
    private static boolean localLoaded;
    /** The connection the synced styles belong to; another connection means "server without the mod (yet)". */
    private static ClientPacketListener syncedConnection;
    private static ClientPacketListener seenConnection;
    private static Field sectionsField;
    private static boolean reflectionBroken;

    /** What we put into a beacon, and what vanilla had there. */
    private static final class Painted {
        List<BeaconBeamOwner.Section> vanilla;
        List<BeaconBeamOwner.Section> ours;
        int height;
        BeamStyle style;
    }

    private static final Map<BlockPos, Painted> PAINTED = new HashMap<>();

    private ClientBeams() {
    }

    // ------------------------------------------------------------ state

    public static void receive(BeamsPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (syncedConnection != minecraft.getConnection()) {
            SYNCED.clear();
            syncedConnection = minecraft.getConnection();
        }
        if (packet.replace()) {
            SYNCED.clear();
        }
        for (BeamsPacket.Entry entry : packet.entries()) {
            SYNCED.put(entry.key(), entry.style());
        }
        MoreBeaconColorsMod.debug("Synced {} beam styles ({} total)", packet.entries().size(), SYNCED.size());
    }

    /** Whether the server we are connected to has the mod. */
    public static boolean serverHasMod() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection == syncedConnection;
    }

    public static BeamStyle style(Level level, BlockPos pos) {
        if (serverHasMod()) {
            return SYNCED.get(syncedKey(level, pos));
        }
        loadLocal();
        return LOCAL.get(localKey(level, pos));
    }

    /** Applies a style: through the server when it has the mod, otherwise locally. */
    public static void set(Level level, BlockPos pos, BeamStyle style) {
        if (serverHasMod()) {
            MycelNetwork.sendToServer(MoreBeaconColorsMod.setBeam, new SetBeamPacket(pos, style));
            return;
        }
        loadLocal();
        if (LOCAL.put(localKey(level, pos), style)) {
            saveLocal();
        }
    }

    private static BeamStore.Key syncedKey(Level level, BlockPos pos) {
        return new BeamStore.Key(level.dimension().identifier().toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    /** Local styles are remembered per server: {@code address|dimension}. */
    private static BeamStore.Key localKey(Level level, BlockPos pos) {
        return new BeamStore.Key(serverId() + "|" + level.dimension().identifier(), pos.getX(), pos.getY(), pos.getZ());
    }

    private static String serverId() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        return server == null ? "local" : server.ip.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static Path localFile() {
        return Mycel.getConfigDir().resolve(MoreBeaconColorsMod.ID + "-local.json");
    }

    private static void loadLocal() {
        if (localLoaded) {
            return;
        }
        localLoaded = true;
        try {
            LOCAL.load(localFile());
        } catch (IOException e) {
            MoreBeaconColorsMod.warn("{}", e.getMessage());
        }
    }

    private static void saveLocal() {
        try {
            LOCAL.save(localFile());
        } catch (IOException e) {
            MoreBeaconColorsMod.warn("Could not save {}: {}", localFile(), e.toString());
        }
    }

    // ------------------------------------------------------------ painting

    /** Called at the end of every client tick. */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection != seenConnection) {
            // New world or server: forget what was painted and synced for the old one.
            seenConnection = connection;
            PAINTED.clear();
            if (connection != syncedConnection) {
                SYNCED.clear();
            }
        }
        Level level = minecraft.level;
        if (level == null || reflectionBroken) {
            return;
        }
        boolean on = MoreBeaconColorsMod.isModEnabled();
        Map<BeamStore.Key, BeamStyle> styles;
        if (serverHasMod()) {
            styles = SYNCED.in(level.dimension().identifier().toString());
        } else {
            loadLocal();
            styles = LOCAL.in(serverId() + "|" + level.dimension().identifier());
        }
        Set<BlockPos> wanted = new HashSet<>();
        if (on) {
            long time = level.getGameTime();
            for (Map.Entry<BeamStore.Key, BeamStyle> entry : styles.entrySet()) {
                BeamStore.Key key = entry.getKey();
                BlockPos pos = new BlockPos(key.x(), key.y(), key.z());
                if (!level.isLoaded(pos)) {
                    continue;
                }
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BeaconBlockEntity beacon) {
                    wanted.add(pos);
                    paint(beacon, pos, entry.getValue(), time);
                }
            }
        }
        // Beacons that went back to vanilla (or were unloaded) get vanilla's list back.
        PAINTED.entrySet().removeIf(entry -> {
            if (wanted.contains(entry.getKey())) {
                return false;
            }
            BlockEntity blockEntity = level.isLoaded(entry.getKey()) ? level.getBlockEntity(entry.getKey()) : null;
            if (blockEntity instanceof BeaconBlockEntity beacon && sections(beacon) == entry.getValue().ours) {
                setSections(beacon, entry.getValue().vanilla);
            }
            return true;
        });
    }

    private static void paint(BeaconBlockEntity beacon, BlockPos pos, BeamStyle style, long time) {
        List<BeaconBeamOwner.Section> current = sections(beacon);
        if (current == null) {
            return;
        }
        Painted painted = PAINTED.get(pos);
        if (painted == null || current != painted.ours) {
            // First time, or vanilla rebuilt its list (every few seconds, or the beam got blocked).
            painted = new Painted();
            painted.vanilla = current;
            painted.height = 0;
            for (BeaconBeamOwner.Section section : current) {
                painted.height += section.getHeight();
            }
            PAINTED.put(pos, painted);
        }
        if (painted.vanilla.isEmpty()) {
            return;
        }
        if (painted.ours != null && !style.animated() && style.equals(painted.style)) {
            return;
        }
        // Vanilla only measures up to the ground surface and stretches the last
        // section into the sky, so gradients and rainbows span up to the build
        // limit: the part of the beam players actually see.
        int visible = painted.height;
        if (beacon.getLevel() != null) {
            visible = Math.max(visible, beacon.getLevel().getMaxY() - pos.getY());
        }
        List<BeaconBeamOwner.Section> ours = new java.util.ArrayList<>();
        for (BeamPainter.Segment segment : BeamPainter.paint(style, visible, time, 0f)) {
            BeaconBeamOwner.Section section = new BeaconBeamOwner.Section(0xFF000000 | segment.rgb());
            for (int i = 1; i < segment.height(); i++) {
                section.increaseHeight();
            }
            ours.add(section);
        }
        painted.ours = ours;
        painted.style = style;
        setSections(beacon, ours);
    }

    @SuppressWarnings("unchecked")
    private static List<BeaconBeamOwner.Section> sections(BeaconBlockEntity beacon) {
        try {
            return (List<BeaconBeamOwner.Section>) field().get(beacon);
        } catch (ReflectiveOperationException | RuntimeException e) {
            broken(e);
            return null;
        }
    }

    private static void setSections(BeaconBlockEntity beacon, List<BeaconBeamOwner.Section> sections) {
        try {
            field().set(beacon, sections);
        } catch (ReflectiveOperationException | RuntimeException e) {
            broken(e);
        }
    }

    private static Field field() throws NoSuchFieldException {
        if (sectionsField == null) {
            Field field = BeaconBlockEntity.class.getDeclaredField("beamSections");
            field.setAccessible(true);
            sectionsField = field;
        }
        return sectionsField;
    }

    private static void broken(Exception e) {
        if (!reflectionBroken) {
            reflectionBroken = true;
            MoreBeaconColorsMod.warn("Can't repaint beacon beams in this Minecraft version: {}", e.toString());
        }
    }
}
