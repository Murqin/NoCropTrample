package com.murqin.nocroptrample.fabric;

import com.murqin.nocroptrample.client.ClientConfigState;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-only Fabric entrypoint for receiving optional read-only server config sync.
 */
public class NoCropTrampleFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientConfigState.setServerConfig(payload.toSnapshot())));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientConfigState.clear());
    }
}
