package my.celium.org.morebeaconcolors.fabric;

import my.celium.org.fabric.FabricNetworking;
import my.celium.org.morebeaconcolors.client.BeamButton;
import my.celium.org.morebeaconcolors.client.ClientBeams;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;

public final class MoreBeaconColorsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricNetworking.flushClient();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientBeams.tick());
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            BeamButton button = BeamButton.create(screen);
            if (button != null) {
                Screens.getWidgets(screen).add(button);
            }
        });
    }
}
