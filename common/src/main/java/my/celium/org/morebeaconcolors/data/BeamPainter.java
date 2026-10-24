package my.celium.org.morebeaconcolors.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a {@link BeamStyle} into beam segments, bottom to top, the way the
 * beacon renderer draws them: each segment is one color for some blocks,
 * and the last one reaches the sky. Pure, so it is unit tested.
 */
public final class BeamPainter {
    /** Blocks per segment in gradients and rainbows: smooth enough, few segments. */
    static final int STEP = 2;
    /** Blocks per full rainbow. */
    static final int RAINBOW_SPAN = 48;
    /** Ticks for one color cycle or pulse at speed 1, 2, 3. */
    static final int[] PERIOD = {0, 400, 160, 60};

    private BeamPainter() {
    }

    /** One run of color: {@code rgb} for {@code height} blocks. */
    public record Segment(int rgb, int height) {
    }

    /**
     * Segments covering {@code height} blocks (at least 1) at game time
     * {@code ticks} (plus {@code partial} for smooth animation). Empty for
     * {@link BeamStyle.Mode#VANILLA}.
     */
    public static List<Segment> paint(BeamStyle style, int height, long ticks, float partial) {
        List<Segment> out = new ArrayList<>();
        int total = Math.max(1, height);
        double time = ticks + partial;
        switch (style.mode()) {
            case VANILLA -> {
            }
            case SOLID -> out.add(new Segment(style.color(), total));
            case GRADIENT -> {
                for (int y = 0; y < total; y += STEP) {
                    float t = total <= STEP ? 0 : (float) (y + STEP / 2) / total;
                    out.add(new Segment(sample(style.used(), t), Math.min(STEP, total - y)));
                }
            }
            case RAINBOW -> {
                double shift = time / PERIOD[style.speed()];
                for (int y = 0; y < total; y += STEP) {
                    float hue = (float) ((double) y / RAINBOW_SPAN - shift);
                    out.add(new Segment(Colors.hsv(hue, 0.85f, 1f), Math.min(STEP, total - y)));
                }
            }
            case CYCLE -> out.add(new Segment(Colors.hsv((float) (time / PERIOD[style.speed()]), 0.85f, 1f), total));
            case PULSE -> {
                double phase = time / PERIOD[style.speed()] * 2 * Math.PI;
                float t = (float) ((1 - Math.cos(phase)) / 2);
                out.add(new Segment(sample(style.used(), t), total));
            }
        }
        return merge(out);
    }

    /** The color at {@code t} (0 to 1) along evenly spaced {@code colors}. */
    static int sample(List<Integer> colors, float t) {
        int n = colors.size();
        if (n == 1) {
            return colors.get(0);
        }
        float p = Math.max(0, Math.min(1, t)) * (n - 1);
        int i = Math.min((int) p, n - 2);
        return Colors.lerp(colors.get(i), colors.get(i + 1), p - i);
    }

    /** Joins neighbours of the same color. */
    static List<Segment> merge(List<Segment> segments) {
        List<Segment> out = new ArrayList<>();
        for (Segment segment : segments) {
            if (!out.isEmpty() && out.get(out.size() - 1).rgb() == segment.rgb()) {
                Segment last = out.remove(out.size() - 1);
                out.add(new Segment(last.rgb(), last.height() + segment.height()));
            } else {
                out.add(segment);
            }
        }
        return out;
    }

    /** The color at the bottom of the beam right now, for previews and swatches. */
    public static int baseColor(BeamStyle style, long ticks, float partial) {
        List<Segment> segments = paint(style, 1, ticks, partial);
        return segments.isEmpty() ? 0xFFFFFF : segments.get(0).rgb();
    }
}
