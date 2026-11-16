package my.celium.org.morebeaconcolors.fabric;

import my.celium.org.fabric.FabricNetworking;
import my.celium.org.morebeaconcolors.MoreBeaconColorsMod;

/**
 * Only loaded when Mycel is installed: bootstrap, then wire the packets Mycel knows about
 * (they may be registered after Mycel's own entrypoint ran).
 */
final class FabricSetup {
    private FabricSetup() {
    }

    static void start() {
        MoreBeaconColorsMod.init();
        FabricNetworking.flush();
    }
}
