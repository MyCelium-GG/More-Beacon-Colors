package my.celium.org.morebeaconcolors.net;

import java.util.ArrayList;
import java.util.List;

import my.celium.org.morebeaconcolors.data.BeamStyle;
import net.minecraft.network.FriendlyByteBuf;

/** Wire format of a {@link BeamStyle}: mode, colors, speed. */
final class Styles {
    private Styles() {
    }

    static void write(FriendlyByteBuf buf, BeamStyle style) {
        buf.writeVarInt(style.mode().ordinal());
        buf.writeVarInt(style.colors().size());
        for (int rgb : style.colors()) {
            buf.writeInt(rgb);
        }
        buf.writeByte(style.speed());
    }

    static BeamStyle read(FriendlyByteBuf buf) {
        int mode = buf.readVarInt();
        BeamStyle.Mode[] modes = BeamStyle.Mode.values();
        int count = buf.readVarInt();
        if (count < 0 || count > BeamStyle.MAX_COLORS) {
            throw new IllegalArgumentException("Bad beam color count " + count);
        }
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            colors.add(buf.readInt());
        }
        return new BeamStyle(mode >= 0 && mode < modes.length ? modes[mode] : BeamStyle.Mode.VANILLA, colors,
                buf.readByte());
    }
}
