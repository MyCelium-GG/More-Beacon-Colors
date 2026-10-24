package my.celium.org.morebeaconcolors.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import my.celium.org.morebeaconcolors.data.BeamPainter.Segment;
import my.celium.org.morebeaconcolors.data.BeamStyle.Mode;

class BeamPainterTest {
    private static int total(List<Segment> segments) {
        return segments.stream().mapToInt(Segment::height).sum();
    }

    @Test
    void vanillaPaintsNothing() {
        assertTrue(BeamPainter.paint(BeamStyle.VANILLA, 100, 0, 0).isEmpty());
    }

    @Test
    void solidIsOneSegment() {
        List<Segment> segments = BeamPainter.paint(BeamStyle.solid(0xFF0000), 250, 7, 0);
        assertEquals(List.of(new Segment(0xFF0000, 250)), segments);
        assertEquals(1, total(BeamPainter.paint(BeamStyle.solid(0xFF0000), 0, 0, 0)), "never empty");
    }

    @Test
    void gradientRunsBottomToTopAndCoversTheBeam() {
        BeamStyle style = new BeamStyle(Mode.GRADIENT, 0x000000, 0xFFFFFF, 2);
        List<Segment> segments = BeamPainter.paint(style, 101, 0, 0);
        assertEquals(101, total(segments));
        int first = segments.get(0).rgb() & 0xFF;
        int last = segments.get(segments.size() - 1).rgb() & 0xFF;
        assertTrue(first < 10 && last > 245, first + " .. " + last);
        for (int i = 1; i < segments.size(); i++) {
            assertTrue((segments.get(i).rgb() & 0xFF) >= (segments.get(i - 1).rgb() & 0xFF), "brightens upward");
        }
    }

    @Test
    void gradientPassesThroughEveryColor() {
        BeamStyle style = new BeamStyle(Mode.GRADIENT, List.of(0xFF0000, 0x00FF00, 0x0000FF), 2);
        List<Segment> segments = BeamPainter.paint(style, 200, 0, 0);
        assertEquals(200, total(segments));
        assertTrue(((segments.get(0).rgb() >> 16) & 0xFF) > 240, "red at the bottom");
        assertTrue((segments.get(segments.size() - 1).rgb() & 0xFF) > 240, "blue at the top");
        int middle = 0;
        int y = 0;
        for (Segment segment : segments) {
            if (y <= 100 && y + segment.height() > 100) {
                middle = segment.rgb();
            }
            y += segment.height();
        }
        assertTrue(((middle >> 8) & 0xFF) > 240, "green halfway: " + Integer.toHexString(middle));
    }

    @Test
    void sampleSpacesColorsEvenly() {
        List<Integer> colors = List.of(0x000000, 0x0000FF, 0x000000);
        assertEquals(0x000000, BeamPainter.sample(colors, 0));
        assertEquals(0x0000FF, BeamPainter.sample(colors, 0.5f));
        assertEquals(0x000000, BeamPainter.sample(colors, 1));
        assertEquals(0x123456, BeamPainter.sample(List.of(0x123456), 0.7f));
    }

    @Test
    void pulseVisitsEveryColor() {
        BeamStyle pulse = new BeamStyle(Mode.PULSE, List.of(0xFF0000, 0x00FF00, 0x0000FF), 2);
        int period = BeamPainter.PERIOD[2];
        assertEquals(0xFF0000, BeamPainter.baseColor(pulse, 0, 0));
        assertEquals(0x0000FF, BeamPainter.baseColor(pulse, period / 2, 0));
    }

    @Test
    void rainbowRisesOverTime() {
        BeamStyle style = new BeamStyle(Mode.RAINBOW, 0, 0, 2);
        List<Segment> now = BeamPainter.paint(style, 96, 0, 0);
        List<Segment> later = BeamPainter.paint(style, 96, 40, 0);
        assertEquals(96, total(now));
        assertTrue(now.size() > 10, "stripes");
        assertNotEquals(now, later);
    }

    @Test
    void cycleAndPulseAnimateTheWholeBeam() {
        BeamStyle cycle = new BeamStyle(Mode.CYCLE, 0, 0, 3);
        assertEquals(1, BeamPainter.paint(cycle, 80, 0, 0).size());
        assertNotEquals(BeamPainter.baseColor(cycle, 0, 0), BeamPainter.baseColor(cycle, 20, 0));

        BeamStyle pulse = new BeamStyle(Mode.PULSE, 0xFF0000, 0x0000FF, 2);
        int period = BeamPainter.PERIOD[2];
        assertEquals(0xFF0000, BeamPainter.baseColor(pulse, 0, 0));
        assertEquals(0x0000FF, BeamPainter.baseColor(pulse, period / 2, 0));
        assertEquals(0xFF0000, BeamPainter.baseColor(pulse, period, 0));
    }

    @Test
    void partialTicksMoveSmoothly() {
        BeamStyle cycle = new BeamStyle(Mode.CYCLE, 0, 0, 1);
        assertNotEquals(BeamPainter.baseColor(cycle, 10, 0f), BeamPainter.baseColor(cycle, 10, 0.9f));
    }

    @Test
    void sameLookIgnoresUnusedColors() {
        BeamStyle a = new BeamStyle(Mode.SOLID, List.of(0x111111, 0x222222), 2);
        BeamStyle b = new BeamStyle(Mode.SOLID, List.of(0x111111, 0x999999), 3);
        assertTrue(a.sameLook(b));
        assertFalse(a.withMode(Mode.GRADIENT).sameLook(b.withMode(Mode.GRADIENT)));
    }

    @Test
    void mergeJoinsEqualNeighbours() {
        List<Segment> merged = BeamPainter.merge(List.of(new Segment(1, 2), new Segment(1, 3), new Segment(2, 1)));
        assertEquals(List.of(new Segment(1, 5), new Segment(2, 1)), merged);
    }

    @Test
    void styleClampsInput() {
        BeamStyle style = new BeamStyle(null, 0xFF123456, 0x1ABCDEF0, 9);
        assertEquals(Mode.VANILLA, style.mode());
        assertEquals(0x123456, style.color());
        assertEquals(0xBCDEF0, style.color2());
        assertEquals(List.of(0xFFFFFF), new BeamStyle(Mode.SOLID, List.of(), 2).colors(), "never empty");
        assertEquals(2, new BeamStyle(Mode.GRADIENT, List.of(0x111111), 2).colors().size(), "padded to the minimum");
        assertEquals(BeamStyle.MAX_COLORS, new BeamStyle(Mode.GRADIENT,
                java.util.Collections.nCopies(40, 0x222222), 2).colors().size(), "capped");
        assertEquals(BeamStyle.MAX_SPEED, style.speed());
        assertEquals(Mode.PULSE, Mode.byName(" Pulse "));
        assertEquals(Mode.VANILLA, Mode.byName("nope"));
    }
}
