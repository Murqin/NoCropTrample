package com.murqin.nocroptrample.fabric;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric entrypoint. Delegates all shared logic to {@link NoCropTrample}.
 */
public class NoCropTrampleFabric implements ModInitializer {
    private static volatile MinecraftServer activeServer;

    @Override
    public void onInitialize() {
        NoCropTrample.init(FabricLoader.getInstance().getConfigDir());

        PayloadTypeRegistry.clientboundPlay().register(SyncConfigPayload.TYPE, SyncConfigPayload.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> activeServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> activeServer = null);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sendIfSupported(handler.getPlayer(), SyncConfigPayload.from(ModConfig.snapshot())));

        ModConfig.setChangeListener(NoCropTrampleFabric::broadcastConfigUpdate);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                NoCropTrampleCommand.register(dispatcher));
    }

    private static void broadcastConfigUpdate(ConfigSnapshot snapshot) {
        MinecraftServer server = activeServer;
        if (server == null) {
            return;
        }
        SyncConfigPayload payload = SyncConfigPayload.from(snapshot);
        server.execute(() -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sendIfSupported(player, payload);
            }
        });
    }

    private static void sendIfSupported(ServerPlayer player, SyncConfigPayload payload) {
        if (ServerPlayNetworking.canSend(player, SyncConfigPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
