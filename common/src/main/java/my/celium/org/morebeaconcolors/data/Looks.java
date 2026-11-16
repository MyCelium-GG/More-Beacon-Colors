package my.celium.org.morebeaconcolors.data;

import java.util.List;

import my.celium.org.morebeaconcolors.data.BeamStyle.Mode;

/** Ready-made beams, one click each in the color menu. Pure data, so it is unit tested. */
public final class Looks {
    public record Look(String name, BeamStyle style) {
    }

    public static final List<Look> ALL = List.of(
            new Look("Candy Cane", new BeamStyle(Mode.STRIPES, List.of(0xE53935, 0xFFFFFF), 1)),
            new Look("Christmas Lights", new BeamStyle(Mode.STRIPES,
                    List.of(0xE53935, 0xFFC107, 0x43A047, 0x1E88E5), 2)),
            new Look("Aurora", new BeamStyle(Mode.AURORA, List.of(0x2BFF88, 0x19C9C2, 0x7B4DFF), 1)),
            new Look("Frost", new BeamStyle(Mode.GRADIENT, List.of(0x4FC3F7, 0xB3E5FC, 0xFFFFFF), 2)),
            new Look("Fire", new BeamStyle(Mode.GRADIENT, List.of(0xB71C1C, 0xFF6F00, 0xFFD54F), 2)),
            new Look("Ocean", new BeamStyle(Mode.AURORA, List.of(0x003B73, 0x0074B7, 0x60A3D9, 0xBFD7ED), 1)),
            new Look("Sunset", new BeamStyle(Mode.GRADIENT, List.of(0xFF8C42, 0xFF3C7E, 0x6A3093), 2)),
            new Look("Pride", new BeamStyle(Mode.GRADIENT,
                    List.of(0xE40303, 0xFF8C00, 0xFFED00, 0x008026, 0x004DFF, 0x750787), 2)));

    private Looks() {
    }
}
