package com.murqin.nocroptrample.fabric;

import com.murqin.nocroptrample.ModGameRules;
import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.networking.v1.ClientboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRule;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Fabric entrypoint. Delegates all shared logic to {@link NoCropTrample}.
 */
public class NoCropTrampleFabric implements ModInitializer {

    private static final Set<MinecraftServer> DIRTY_SERVERS =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    @Override
    public void onInitialize() {
        NoCropTrample.init(FabricLoader.getInstance().getConfigDir());

        ModGameRules.registerAll((id, rule) -> Registry.register(BuiltInRegistries.GAME_RULE, id, rule));
        for (GameRule<?> rule : ModGameRules.allRules()) {
            registerChangeCallback(rule);
        }

        PayloadTypeRegistry.clientboundPlay().register(SyncConfigPayload.TYPE, SyncConfigPayload.STREAM_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sendIfSupported(handler.getPlayer(), SyncConfigPayload.from(ModGameRules.snapshot(server.getGameRules()))));

        ClientboundPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(SyncConfigPayload.TYPE.id())) {
                sendIfSupported(handler.getPlayer(), SyncConfigPayload.from(ModGameRules.snapshot(server.getGameRules())));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (DIRTY_SERVERS.remove(server)) {
                broadcastConfigUpdate(server, ModGameRules.snapshot(server.getGameRules()));
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(DIRTY_SERVERS::remove);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                NoCropTrampleCommand.register(dispatcher));
    }

    private static <T> void registerChangeCallback(GameRule<T> rule) {
        GameRuleEvents.changeCallback(rule).register((value, server) -> {
            if (server != null) {
                DIRTY_SERVERS.add(server);
            }
        });
    }

    private static void broadcastConfigUpdate(MinecraftServer server, ConfigSnapshot snapshot) {
        if (server == null || server.getPlayerList() == null) {
            return;
        }
        SyncConfigPayload payload = SyncConfigPayload.from(snapshot);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendIfSupported(player, payload);
        }
    }

    private static void sendIfSupported(ServerPlayer player, SyncConfigPayload payload) {
        if (ServerPlayNetworking.canSend(player, SyncConfigPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
