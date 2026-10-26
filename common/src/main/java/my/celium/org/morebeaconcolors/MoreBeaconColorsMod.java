package my.celium.org.morebeaconcolors;

import my.celium.org.Mycel;
import my.celium.org.config.ConfigBuilder;
import my.celium.org.config.ConfigValue;
import my.celium.org.config.MycelConfig;
import my.celium.org.event.MycelEvents;
import my.celium.org.event.ServerLifecycleEvent;
import my.celium.org.logging.MycelLog;
import my.celium.org.metadata.ModMetadata;
import my.celium.org.morebeaconcolors.net.BeamsPacket;
import my.celium.org.morebeaconcolors.net.SetBeamPacket;
import my.celium.org.morebeaconcolors.server.BeaconColors;
import my.celium.org.network.MycelNetwork;
import my.celium.org.network.PacketDefinition;

/**
 * More Beacon Colors: a button on the beacon screen opens a color menu for
 * the beam: any solid color, gradients, rainbows, color cycles and pulses.
 *
 * <p>The server keeps each beacon's style in the world folder and tells every
 * player with the mod; their clients repaint the beam. On a server without
 * the mod, the client remembers colors for itself (only you see them).
 * Vanilla players see vanilla beams.
 */
public final class MoreBeaconColorsMod {
    public static final String ID = "morebeaconcolors";

    public static final ModMetadata META = ModMetadata.builder(ID, "More Beacon Colors", "1.0.1")
            .author("MyCelium")
            .website("https://www.curseforge.com/minecraft/mc-mods/more-beacon-colors")
            .updateUrl("https://wiki.mycelium-gg.org/updates/morebeaconcolors.json")
            .build();

    public static ConfigValue<Boolean> enabled;
    public static ConfigValue<Boolean> showButton;
    public static ConfigValue<Boolean> allowAnimated;
    public static ConfigValue<Boolean> debug;
    public static MycelConfig config;

    public static PacketDefinition<SetBeamPacket> setBeam;
    public static PacketDefinition<BeamsPacket> beams;

    private MoreBeaconColorsMod() {
    }

    /** Called once from each loader's entrypoint. */
    public static void init() {
        Mycel.initialize();
        Mycel.registerMod(META);

        ConfigBuilder builder = Mycel.config(META);
        enabled = builder.booleanValue("enabled", true,
                "Whether the mod does anything at all.");
        showButton = builder.booleanValue("showButton", true,
                "Show the color button on the beacon screen (client).");
        allowAnimated = builder.booleanValue("allowAnimated", true,
                "Allow rainbow, color cycle and pulse beams (server).");
        debug = builder.booleanValue("debug", false,
                "Log color changes, syncing and clean-ups to the game log.");
        config = builder.build();

        setBeam = PacketDefinition.toServer(SetBeamPacket.TYPE, SetBeamPacket.CODEC,
                (packet, context) -> BeaconColors.onSetBeam(packet, context.sender()));
        beams = PacketDefinition.toClient(BeamsPacket.TYPE, BeamsPacket.CODEC, BeamsPacket::receive);
        // Mycel 1.4.0+ registers both channels as optional on every loader,
        // so players with the mod can join servers without it and the other
        // way round; senders skip clients without the channel.
        MycelNetwork.register(META, setBeam);
        MycelNetwork.register(META, beams);

        MycelEvents.onServerLifecycle(event -> {
            if (event.phase() == ServerLifecycleEvent.Phase.STARTED) {
                BeaconColors.start(event.server());
            } else {
                BeaconColors.stop();
            }
        });
        MycelEvents.onPlayerJoin(event -> BeaconColors.sendAll(event.player()));
    }

    /** Info-level log line tagged {@code [debug]}, only while the {@code debug} option is on. */
    public static void debug(String message, Object... args) {
        try {
            if (debug == null || !debug.get()) {
                return;
            }
            MycelLog.infoMod(ID, "[debug] " + message, args);
        } catch (RuntimeException e) {
            // Logging must never break the caller.
        }
    }

    public static void warn(String message, Object... args) {
        try {
            MycelLog.warnMod(ID, message, args);
        } catch (RuntimeException e) {
            // Logging must never break the caller.
        }
    }

    public static boolean isModEnabled() {
        return Mycel.isEnabled(ID) && enabled.get();
    }
}
