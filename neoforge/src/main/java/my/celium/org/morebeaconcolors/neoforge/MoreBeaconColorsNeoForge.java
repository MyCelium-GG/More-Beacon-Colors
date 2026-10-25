package my.celium.org.morebeaconcolors.neoforge;

import my.celium.org.client.ClientHooks;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge entrypoint. Mycel wires the packets (optional both ways); client
 * hooks live in {@link ClientMoreBeaconColors}.
 */
@Mod(MoreBeaconColorsMod.ID)
public final class MoreBeaconColorsNeoForge {
    public MoreBeaconColorsNeoForge(IEventBus modBus, @SuppressWarnings("unused") ModContainer container) {
        MoreBeaconColorsMod.init();
        if (FMLEnvironment.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> (ModContainer ignored, Screen parent) -> ClientHooks.createConfigScreen(parent,
                            MoreBeaconColorsMod.ID));
            ClientMoreBeaconColors.register();
        }
    }
}
