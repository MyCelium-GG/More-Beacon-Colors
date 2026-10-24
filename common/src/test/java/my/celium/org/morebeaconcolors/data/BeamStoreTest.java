package my.celium.org.morebeaconcolors.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import my.celium.org.morebeaconcolors.data.BeamStyle.Mode;

class BeamStoreTest {
    private static final BeamStore.Key HOME = new BeamStore.Key("minecraft:overworld", 10, 64, -3);
    private static final BeamStore.Key NETHER = new BeamStore.Key("minecraft:the_nether", 0, 70, 0);

    @Test
    void vanillaIsNeverStored() {
        BeamStore store = new BeamStore();
        assertTrue(store.put(HOME, BeamStyle.solid(0xFF0000)));
        assertFalse(store.put(HOME, BeamStyle.solid(0xFF0000)), "same style is no change");
        assertTrue(store.put(HOME, BeamStyle.VANILLA), "vanilla removes");
        assertEquals(0, store.size());
        assertEquals(BeamStyle.VANILLA, store.get(HOME));
        assertFalse(store.put(HOME, BeamStyle.VANILLA));
    }

    @Test
    void filtersByDimension() {
        BeamStore store = new BeamStore();
        store.put(HOME, BeamStyle.solid(1));
        store.put(NETHER, BeamStyle.solid(2));
        assertEquals(1, store.in("minecraft:the_nether").size());
        assertEquals(BeamStyle.solid(2), store.in("minecraft:the_nether").get(NETHER));
    }

    @Test
    void jsonRoundTripsAndIsReadable() {
        BeamStore store = new BeamStore();
        store.put(HOME, new BeamStyle(Mode.GRADIENT, 0x3AB3DA, 0xB388FF, 2));
        store.put(NETHER, new BeamStyle(Mode.RAINBOW, 0, 0, 3));
        String json = store.toJson();
        assertTrue(json.contains("\"mode\": \"gradient\""), json);
        assertTrue(json.contains("\"#3AB3DA\""), json);

        BeamStore back = new BeamStore();
        assertEquals(2, back.fromJson(json));
        assertEquals(store.all(), back.all());
    }

    @Test
    void manyColorsRoundTrip() {
        BeamStore store = new BeamStore();
        store.put(HOME, new BeamStyle(Mode.GRADIENT, List.of(0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF), 2));
        BeamStore back = new BeamStore();
        back.fromJson(store.toJson());
        assertEquals(List.of(0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF), back.get(HOME).colors());
    }

    @Test
    void oldTwoColorFilesStillLoad() {
        BeamStore store = new BeamStore();
        store.fromJson("""
                {"format": 1, "beacons": [{"dimension": "minecraft:overworld", "x": 1, "y": 2, "z": 3,
                  "mode": "gradient", "color": "#112233", "color2": "#445566", "speed": 2}]}""");
        assertEquals(List.of(0x112233, 0x445566), store.get(new BeamStore.Key("minecraft:overworld", 1, 2, 3)).colors());
    }

    @Test
    void handEditedFilesAreForgiving() {
        BeamStore store = new BeamStore();
        int read = store.fromJson("""
                {"beacons": [
                  {"dimension": "minecraft:overworld", "x": 1, "y": 2, "z": 3, "mode": "SOLID", "color": "f00"},
                  {"dimension": "", "x": 0, "y": 0, "z": 0, "mode": "solid", "color": "#00ff00"},
                  {"dimension": "minecraft:overworld", "x": 4, "y": 5, "z": 6, "mode": "vanilla"},
                  {"dimension": "minecraft:overworld", "x": 7, "y": 8, "z": 9, "mode": "pulse", "color": "nope", "speed": 99}
                ]}""");
        assertEquals(2, read);
        BeamStyle solid = store.get(new BeamStore.Key("minecraft:overworld", 1, 2, 3));
        assertEquals(BeamStyle.solid(0xFF0000), solid);
        BeamStyle pulse = store.get(new BeamStore.Key("minecraft:overworld", 7, 8, 9));
        assertEquals(0xFFFFFF, pulse.color());
        assertEquals(3, pulse.speed());
        assertThrows(IllegalArgumentException.class, () -> store.fromJson("{ nope"));
    }

    @Test
    void fileRoundTripAndBrokenFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("data/morebeaconcolors.json");
        BeamStore store = new BeamStore();
        assertFalse(store.load(file));
        store.put(HOME, BeamStyle.solid(0x123456));
        store.save(file);
        assertFalse(Files.exists(dir.resolve("data/morebeaconcolors.json.tmp")));

        BeamStore back = new BeamStore();
        assertTrue(back.load(file));
        assertEquals(BeamStyle.solid(0x123456), back.get(HOME));

        Files.writeString(file, "{ broken");
        assertThrows(IOException.class, () -> back.load(file));
        assertTrue(Files.exists(dir.resolve("data/morebeaconcolors.json.broken")));
        assertEquals(0, back.size());
    }
}
