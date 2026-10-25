package my.celium.org.morebeaconcolors.fabric;

import my.celium.org.fabric.FabricNetworking;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint: bootstrap, then wire the packets Mycel knows about
 * (they may be registered after Mycel's own entrypoint ran).
 */
public final class MoreBeaconColorsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoreBeaconColorsMod.init();
        FabricNetworking.flush();
    }
}
