package my.celium.org.morebeaconcolors.neoforge;

import my.celium.org.client.ClientHooks;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Only loaded when Mycel is installed. Mycel wires the packets (optional both ways); client hooks live
 * in {@link ClientMoreBeaconColors}.
 */
final class NeoForgeSetup {
    private NeoForgeSetup() {
    }

    static void start() {
        MoreBeaconColorsMod.init();
        if (FMLEnvironment.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> (ModContainer ignored, Screen parent) -> ClientHooks.createConfigScreen(parent,
                            MoreBeaconColorsMod.ID));
            ClientMoreBeaconColors.register();
        }
    }
}
