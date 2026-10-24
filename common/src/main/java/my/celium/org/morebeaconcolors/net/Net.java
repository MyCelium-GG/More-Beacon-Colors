package my.celium.org.morebeaconcolors.net;

import java.util.function.BiPredicate;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sending to players. Packets travel as plain vanilla custom payloads; each
 * loader module says who can receive which channel, so players without the
 * mod (or vanilla clients) are simply skipped.
 */
public final class Net {
    /** Set by the loader module; everyone by default. */
    private static BiPredicate<ServerPlayer, CustomPacketPayload.Type<?>> canReceive = (player, type) -> true;

    private Net() {
    }

    public static void setCanReceive(BiPredicate<ServerPlayer, CustomPacketPayload.Type<?>> test) {
        canReceive = test;
    }

    /** Sends unless the player's client lacks the channel. Returns whether it was sent. */
    public static boolean send(ServerPlayer player, CustomPacketPayload payload) {
        try {
            if (!canReceive.test(player, payload.type())) {
                return false;
            }
        } catch (RuntimeException e) {
            return false;
        }
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
        return true;
    }
}
