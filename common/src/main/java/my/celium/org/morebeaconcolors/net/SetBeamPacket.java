package my.celium.org.morebeaconcolors.net;

import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import my.celium.org.morebeaconcolors.data.BeamStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client to server: "make the beacon I have open look like this". The
 * server checks the open beacon menu itself; {@code pos} is only a fallback.
 */
public record SetBeamPacket(BlockPos pos, BeamStyle style) implements CustomPacketPayload {
    public static final Type<SetBeamPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MoreBeaconColorsMod.ID, "set_beam"));
    public static final StreamCodec<FriendlyByteBuf, SetBeamPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                Styles.write(buf, packet.style());
            },
            buf -> new SetBeamPacket(buf.readBlockPos(), Styles.read(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
