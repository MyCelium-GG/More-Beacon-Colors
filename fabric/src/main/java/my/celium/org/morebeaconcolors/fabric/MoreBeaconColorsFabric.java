package my.celium.org.morebeaconcolors.fabric;

import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import my.celium.org.morebeaconcolors.net.BeamsPacket;
import my.celium.org.morebeaconcolors.net.Net;
import my.celium.org.morebeaconcolors.net.SetBeamPacket;
import my.celium.org.morebeaconcolors.server.BeaconColors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Fabric entrypoint: bootstrap and the server half of networking. Payload
 * types are registered here once (the client entrypoint only adds its
 * receiver), so nothing is registered twice.
 */
public final class MoreBeaconColorsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoreBeaconColorsMod.init();
        PayloadTypeRegistry.serverboundPlay().register(SetBeamPacket.TYPE, SetBeamPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BeamsPacket.TYPE, BeamsPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SetBeamPacket.TYPE,
                (payload, context) -> context.server().execute(() -> BeaconColors.onSetBeam(payload, context.player())));
        Net.setCanReceive(ServerPlayNetworking::canSend);
    }
}
