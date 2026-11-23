package com.murqin.nocroptrample.fabric;

import com.murqin.nocroptrample.client.ClientConfigState;
import com.murqin.nocroptrample.client.CreateWorldDefaultsHelper;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

/**
 * Client-only Fabric entrypoint for receiving optional read-only server config sync
 * and seeding newly created worlds with updated default settings.
 */
public class NoCropTrampleFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> ClientConfigState.clear());
        ClientPlayConnectionEvents.INIT.register((handler, client) -> ClientConfigState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientConfigState.clear());

        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPayload.TYPE, (payload, context) -> {
            long generation = ClientConfigState.currentGeneration();
            ConfigSnapshot snapshot = payload.toSnapshot();
            context.client().execute(() -> {
                if (context.client().getConnection() != null) {
                    ClientConfigState.setServerConfigIfGeneration(snapshot, generation);
                }
            });
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                CreateWorldDefaultsHelper.applyIfCreateWorldScreen(screen));
    }
}
