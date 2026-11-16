package my.celium.org.morebeaconcolors.fabric;

import my.celium.org.morebeaconcolors.MissingMycel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric entrypoint. Without Mycel the game still starts: More Beacon Colors stays off and tells every
 * player who joins. Everything that needs Mycel lives in {@link FabricSetup}.
 */
public final class MoreBeaconColorsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("mycel")) {
            MissingMycel.logOnce();
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> MissingMycel.tell(handler.getPlayer()));
            return;
        }
        FabricSetup.start();
    }
}
