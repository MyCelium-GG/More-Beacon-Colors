package my.celium.org.morebeaconcolors.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A beam as one line of text, for copy and paste between beacons and for sharing in chat:
 * {@code beam:stripes:1:E53935,FFFFFF} (style, speed, colors). Pure, so it is unit tested.
 */
public final class StyleCode {
    static final String PREFIX = "beam:";

    private StyleCode() {
    }

    public static String write(BeamStyle style) {
        StringBuilder out = new StringBuilder(PREFIX).append(style.mode().id()).append(':').append(style.speed())
                .append(':');
        List<Integer> colors = style.used().isEmpty() ? style.colors() : style.used();
        for (int i = 0; i < colors.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(String.format(Locale.ROOT, "%06X", colors.get(i)));
        }
        return out.toString();
    }

    /**
     * The beam in {@code text}, which may have other text around it (a chat line), or null when there is
     * no beam code in it.
     */
    public static BeamStyle read(String text) {
        if (text == null) {
            return null;
        }
        int start = text.toLowerCase(Locale.ROOT).indexOf(PREFIX);
        if (start < 0) {
            return null;
        }
        String[] parts = text.substring(start + PREFIX.length()).trim().split("\\s", 2)[0].split(":", -1);
        if (parts.length != 3) {
            return null;
        }
        BeamStyle.Mode mode = BeamStyle.Mode.byName(parts[0]);
        if (!mode.name().equalsIgnoreCase(parts[0].trim())) {
            return null;
        }
        int speed;
        try {
            speed = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return null;
        }
        List<Integer> colors = new ArrayList<>();
        for (String hex : parts[2].split(",")) {
            int rgb = Colors.parseHex(hex.trim());
            if (rgb < 0) {
                return null;
            }
            colors.add(rgb);
        }
        if (colors.size() > BeamStyle.MAX_COLORS) {
            return null;
        }
        return new BeamStyle(mode, colors, speed);
    }
}
