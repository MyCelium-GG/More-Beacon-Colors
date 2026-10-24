package my.celium.org.morebeaconcolors.neoforge;

import my.celium.org.morebeaconcolors.client.BeamButton;
import my.celium.org.morebeaconcolors.client.ClientBeams;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientMoreBeaconColors {
    private ClientMoreBeaconColors() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClientMoreBeaconColors::onScreenInit);
        NeoForge.EVENT_BUS.addListener(ClientMoreBeaconColors::onClientTick);
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        BeamButton button = BeamButton.create(event.getScreen());
        if (button != null) {
            event.addListener(button);
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        ClientBeams.tick();
    }
}
