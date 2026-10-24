package my.celium.org.morebeaconcolors.forge;

import my.celium.org.client.ClientHooks;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entrypoint: packets travel on Mycel's shared channel (registered in
 * {@link MoreBeaconColorsMod#init()}); client hooks live in
 * {@link ClientMoreBeaconColors} so dedicated servers never load client classes.
 */
@Mod(MoreBeaconColorsMod.ID)
public final class MoreBeaconColorsForge {
    public MoreBeaconColorsForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        MoreBeaconColorsMod.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (Minecraft minecraft, Screen parent) -> ClientHooks.createConfigScreen(parent,
                                    MoreBeaconColorsMod.ID)));
            ClientMoreBeaconColors.register();
        }
    }
}
