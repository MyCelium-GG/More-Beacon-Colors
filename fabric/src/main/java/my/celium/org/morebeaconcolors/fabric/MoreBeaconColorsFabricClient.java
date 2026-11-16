package my.celium.org.morebeaconcolors.fabric;

import my.celium.org.morebeaconcolors.MissingMycel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric client entrypoint. Without Mycel the game still starts: the mod stays off and says so in chat
 * when joining a server (singleplayer worlds get the message from {@link MoreBeaconColorsFabric}).
 */
public final class MoreBeaconColorsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (!FabricLoader.getInstance().isModLoaded("mycel")) {
            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                if (!client.isLocalServer() && client.player != null) {
                    client.player.sendSystemMessage(MissingMycel.message());
                }
            });
            return;
        }
        FabricClientSetup.start();
    }
}
