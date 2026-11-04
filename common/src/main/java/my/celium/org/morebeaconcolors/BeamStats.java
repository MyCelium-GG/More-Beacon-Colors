package my.celium.org.morebeaconcolors;

import my.celium.org.stats.ModStats;

/**
 * Anonymous stats for the FastStats project {@code more-beacon-colors}, sent through Mycel under its
 * consent: how many beams are colored and in which modes, which modes get picked, and the settings.
 */
public final class BeamStats {
    private static final String TOKEN = "ab91f080177564a24902deaa71dfb6c7";

    private static volatile ModStats stats;

    private BeamStats() {
    }

    static void start() {
        stats = ModStats.builder(MoreBeaconColorsMod.META, TOKEN)
                .bool("enabled", () -> MoreBeaconColorsMod.enabled.get())
                .bool("show_button", () -> MoreBeaconColorsMod.showButton.get())
                .bool("allow_animated", () -> MoreBeaconColorsMod.allowAnimated.get())
                .number("colored_beams", my.celium.org.morebeaconcolors.server.BeaconColors::coloredCount)
                .numberMap("beam_modes", my.celium.org.morebeaconcolors.server.BeaconColors::modeCounts)
                .counters("chosen_modes")
                .errors("my.celium.org.morebeaconcolors.")
                .start();
    }

    /** Counts a feature use (sent as {@code usage}). */
    public static void count(String what) {
        ModStats current = stats;
        if (current != null) {
            current.count(what);
        }
    }

    /** Counts {@code key} in the counter map {@code id}. */
    public static void count(String id, String key) {
        ModStats current = stats;
        if (current != null && key != null) {
            current.count(id, key.startsWith("minecraft:") ? key.substring(10) : key);
        }
    }
}
