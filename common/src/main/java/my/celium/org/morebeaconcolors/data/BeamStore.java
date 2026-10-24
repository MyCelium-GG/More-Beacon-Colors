package my.celium.org.morebeaconcolors.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Every colored beacon: dimension id, block position, style. Vanilla beacons
 * are never stored. Saved as hand-editable JSON (colors as {@code #RRGGBB}).
 * Files from before multi-color gradients ({@code color}, {@code color2})
 * still load.
 * Pure Java, so it is unit tested; the server and the client's offline
 * fallback each own one.
 */
public final class BeamStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** A beacon position in a dimension. */
    public record Key(String dimension, int x, int y, int z) {
        public Key {
            Objects.requireNonNull(dimension, "dimension");
        }
    }

    private final Map<Key, BeamStyle> styles = new LinkedHashMap<>();

    public BeamStyle get(Key key) {
        return styles.getOrDefault(key, BeamStyle.VANILLA);
    }

    /** Stores {@code style}; vanilla removes the entry. Returns whether anything changed. */
    public boolean put(Key key, BeamStyle style) {
        if (style == null || style.isVanilla()) {
            return styles.remove(key) != null;
        }
        return !style.equals(styles.put(key, style));
    }

    public boolean remove(Key key) {
        return styles.remove(key) != null;
    }

    public void clear() {
        styles.clear();
    }

    public int size() {
        return styles.size();
    }

    /** All entries, in insertion order. */
    public Map<Key, BeamStyle> all() {
        return java.util.Collections.unmodifiableMap(styles);
    }

    /** Entries of one dimension. */
    public Map<Key, BeamStyle> in(String dimension) {
        Map<Key, BeamStyle> out = new LinkedHashMap<>();
        styles.forEach((key, style) -> {
            if (key.dimension().equals(dimension)) {
                out.put(key, style);
            }
        });
        return out;
    }

    // ------------------------------------------------------------ JSON

    /** The file layout: one object per beacon. */
    static final class FileData {
        int format = 2;
        List<Entry> beacons = new ArrayList<>();
    }

    static final class Entry {
        String dimension;
        int x;
        int y;
        int z;
        String mode;
        List<String> colors;
        /** Older files: first and second color instead of {@code colors}. */
        String color;
        String color2;
        int speed = 2;
    }

    public String toJson() {
        FileData data = new FileData();
        styles.forEach((key, style) -> {
            Entry entry = new Entry();
            entry.dimension = key.dimension();
            entry.x = key.x();
            entry.y = key.y();
            entry.z = key.z();
            entry.mode = style.mode().id();
            entry.colors = style.colors().stream().map(Colors::toHex).toList();
            entry.speed = style.speed();
            data.beacons.add(entry);
        });
        return GSON.toJson(data);
    }

    /** Replaces the contents with {@code json}; entries that don't make sense are skipped. Returns how many were read. */
    public int fromJson(String json) {
        styles.clear();
        FileData data;
        try {
            data = GSON.fromJson(json, FileData.class);
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalArgumentException("Not a beacon color file: " + e.getMessage(), e);
        }
        if (data == null || data.beacons == null) {
            return 0;
        }
        for (Entry entry : data.beacons) {
            if (entry == null || entry.dimension == null || entry.dimension.isBlank()) {
                continue;
            }
            List<Integer> colors = new ArrayList<>();
            for (String hex : entry.colors != null ? entry.colors : java.util.Arrays.asList(entry.color, entry.color2)) {
                int rgb = Colors.parseHex(hex);
                if (rgb >= 0) {
                    colors.add(rgb);
                }
            }
            BeamStyle style = new BeamStyle(BeamStyle.Mode.byName(entry.mode), colors, entry.speed);
            put(new Key(entry.dimension.trim(), entry.x, entry.y, entry.z), style);
        }
        return styles.size();
    }

    /** Loads {@code file} if it exists; returns false when there was none. A broken file is kept as {@code .broken}. */
    public boolean load(Path file) throws IOException {
        styles.clear();
        if (!Files.isRegularFile(file)) {
            return false;
        }
        String json = Files.readString(file, StandardCharsets.UTF_8);
        try {
            fromJson(json);
            return true;
        } catch (IllegalArgumentException e) {
            Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
            throw new IOException("Unreadable " + file.getFileName() + ", kept as .broken", e);
        }
    }

    /** Writes atomically (temp file, then one move). */
    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, toJson(), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
