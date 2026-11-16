package my.celium.org.morebeaconcolors.forge;

import my.celium.org.morebeaconcolors.MissingMycel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entrypoint. Without Mycel the game still starts: More Beacon Colors stays off and says so in
 * chat. Everything that needs Mycel lives in {@link ForgeSetup}.
 */
@Mod("morebeaconcolors")
public final class MoreBeaconColorsForge {
    public MoreBeaconColorsForge(@SuppressWarnings("unused") FMLJavaModLoadingContext context) {
        if (!ModList.isLoaded("mycel")) {
            MissingMycel.logOnce();
            PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
                if (event.getEntity() instanceof ServerPlayer player) {
                    MissingMycel.tell(player);
                }
            });
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientMissingMycel.register();
            }
            return;
        }
        ForgeSetup.start();
    }
}
