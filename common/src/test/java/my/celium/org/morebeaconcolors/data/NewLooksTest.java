package my.celium.org.morebeaconcolors.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import my.celium.org.morebeaconcolors.data.BeamPainter.Segment;
import my.celium.org.morebeaconcolors.data.BeamStyle.Mode;

class NewLooksTest {
    private static int total(List<Segment> segments) {
        return segments.stream().mapToInt(Segment::height).sum();
    }

    private static int colorAt(List<Segment> segments, int y) {
        int at = 0;
        for (Segment segment : segments) {
            if (y < at + segment.height()) {
                return segment.rgb();
            }
            at += segment.height();
        }
        throw new AssertionError("beyond the beam");
    }

    @Test
    void stripesRepeatTheColorsInOrder() {
        BeamStyle candy = new BeamStyle(Mode.STRIPES, List.of(0xFF0000, 0xFFFFFF), 1);
        List<Segment> segments = BeamPainter.paint(candy, 60, 0, 0);
        assertEquals(60, total(segments));
        int s = BeamPainter.STRIPE;
        assertEquals(0xFF0000, colorAt(segments, 0));
        assertEquals(0xFFFFFF, colorAt(segments, s));
        assertEquals(0xFF0000, colorAt(segments, 2 * s));
        assertEquals(0xFFFFFF, colorAt(segments, 3 * s + s - 1));
    }

    @Test
    void stripesRiseAndGlide() {
        BeamStyle candy = new BeamStyle(Mode.STRIPES, List.of(0xFF0000, 0x0000FF), 2);
        int period = BeamPainter.PERIOD[2];
        // After the stripes rose exactly one stripe, the colors have swapped places.
        long oneStripe = (long) period * BeamPainter.STRIPE / BeamPainter.STRIPE_RISE;
        assertEquals(0x0000FF, colorAt(BeamPainter.paint(candy, 30, oneStripe, 0), 0));
        // Halfway through a block, the edge block mixes both.
        long halfBlock = (long) period / BeamPainter.STRIPE_RISE / 2;
        int mixed = colorAt(BeamPainter.paint(candy, 30, halfBlock, 0), 0);
        assertTrue((mixed >> 16) > 0x40 && (mixed & 0xFF) > 0x40, Integer.toHexString(mixed));
    }

    @Test
    void auroraStaysInsideItsColorsAndMoves() {
        BeamStyle aurora = new BeamStyle(Mode.AURORA, List.of(0x000000, 0x0000FF), 1);
        List<Segment> now = BeamPainter.paint(aurora, 200, 0, 0);
        assertEquals(200, total(now));
        for (Segment segment : now) {
            assertEquals(0, segment.rgb() >> 8, "only blues: " + Integer.toHexString(segment.rgb()));
        }
        assertNotEquals(now, BeamPainter.paint(aurora, 200, 300, 0));
    }

    @Test
    void newModesAreAnimatedAndAppendedLast() {
        Mode[] modes = Mode.values();
        assertEquals(Mode.PULSE, modes[5], "older versions read modes by number");
        assertEquals(Mode.STRIPES, modes[6]);
        assertEquals(Mode.AURORA, modes[7]);
        assertTrue(Mode.STRIPES.animated && Mode.AURORA.animated);
    }

    @Test
    void codesRoundTrip() {
        for (Looks.Look look : Looks.ALL) {
            assertTrue(look.style().sameLook(StyleCode.read(StyleCode.write(look.style()))), look.name());
        }
        assertEquals("beam:stripes:1:E53935,FFFFFF", StyleCode.write(Looks.ALL.get(0).style()));
        BeamStyle solid = BeamStyle.solid(0x123456);
        assertTrue(solid.sameLook(StyleCode.read(StyleCode.write(solid))));
    }

    @Test
    void codesAreFoundInChatLines() {
        BeamStyle read = StyleCode.read("<Steve> try this one: beam:aurora:3:2BFF88,7B4DFF  it looks great");
        assertEquals(Mode.AURORA, read.mode());
        assertEquals(3, read.speed());
        assertEquals(List.of(0x2BFF88, 0x7B4DFF), read.colors());
        assertEquals(Mode.GRADIENT, StyleCode.read("BEAM:Gradient:2:#FF0000,00F").mode());
    }

    @Test
    void badCodesAreIgnored() {
        assertNull(StyleCode.read(null));
        assertNull(StyleCode.read("hello"));
        assertNull(StyleCode.read("beam:sparkles:2:FF0000"));
        assertNull(StyleCode.read("beam:solid:x:FF0000"));
        assertNull(StyleCode.read("beam:solid:2:GG0000"));
        assertNull(StyleCode.read("beam:solid:2:"));
        assertNull(StyleCode.read("beam:solid:2"));
    }

    @Test
    void looksHaveDistinctNames() {
        assertEquals(Looks.ALL.size(), Looks.ALL.stream().map(Looks.Look::name).distinct().count());
    }
}
