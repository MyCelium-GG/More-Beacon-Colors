package my.celium.org.morebeaconcolors.forge;

import my.celium.org.morebeaconcolors.client.BeamButton;
import my.celium.org.morebeaconcolors.client.ClientBeams;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;

public final class ClientMoreBeaconColors {
    private ClientMoreBeaconColors() {
    }

    public static void register() {
        ScreenEvent.Init.Post.BUS.addListener(ClientMoreBeaconColors::onScreenInit);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ClientMoreBeaconColors::onClientTick);
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        BeamButton button = BeamButton.create(event.getScreen());
        if (button != null) {
            event.addListener(button);
        }
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        ClientBeams.tick();
    }
}
