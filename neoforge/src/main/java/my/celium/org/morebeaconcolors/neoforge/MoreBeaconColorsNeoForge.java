package my.celium.org.morebeaconcolors.neoforge;

import my.celium.org.client.ClientHooks;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import my.celium.org.morebeaconcolors.net.BeamsPacket;
import my.celium.org.morebeaconcolors.net.Net;
import my.celium.org.morebeaconcolors.net.SetBeamPacket;
import my.celium.org.morebeaconcolors.server.BeaconColors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge entrypoint. Registers both packets as optional, so players with
 * the mod can join servers without it and the other way round; client hooks
 * live in {@link ClientMoreBeaconColors}.
 */
@Mod(MoreBeaconColorsMod.ID)
public final class MoreBeaconColorsNeoForge {
    public MoreBeaconColorsNeoForge(IEventBus modBus, @SuppressWarnings("unused") ModContainer container) {
        MoreBeaconColorsMod.init();
        modBus.addListener(MoreBeaconColorsNeoForge::registerPayloads);
        Net.setCanReceive((player, type) -> NetworkRegistry.hasChannel(player.connection, type.id()));
        if (FMLEnvironment.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> (ModContainer ignored, Screen parent) -> ClientHooks.createConfigScreen(parent,
                            MoreBeaconColorsMod.ID));
            ClientMoreBeaconColors.register();
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(SetBeamPacket.TYPE, SetBeamPacket.CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> BeaconColors.onSetBeam(payload, (ServerPlayer) context.player())));
        registrar.playToClient(BeamsPacket.TYPE, BeamsPacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> BeamsPacket.receive(payload, null)));
    }
}
