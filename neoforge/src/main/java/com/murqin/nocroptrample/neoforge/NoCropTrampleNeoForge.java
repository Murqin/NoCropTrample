package com.murqin.nocroptrample.neoforge;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.TrampleRules;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * NeoForge entrypoint. Delegates all shared logic to {@link NoCropTrample}.
 * <p>
 * Uses NeoForge's native {@link BlockEvent.FarmlandTrampleEvent} instead of a
 * mixin, so this side isn't affected by Mojang renaming FarmlandBlock's
 * internal methods, and stays compatible with other mods listening on the
 * same event.
 * </p>
 */
@Mod(NoCropTrample.MOD_ID)
public class NoCropTrampleNeoForge {

    public NoCropTrampleNeoForge(IEventBus modBus) {
        NoCropTrample.init(FMLPaths.CONFIGDIR.get());

        modBus.addListener(NoCropTrampleNeoForge::onRegisterPayloads);

        ModConfig.setChangeListener(NoCropTrampleNeoForge::broadcastConfigUpdate);

        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onFarmlandTrample);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onPlayerLoggedIn);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .optional()
                .playToClient(SyncConfigPayload.TYPE, SyncConfigPayload.STREAM_CODEC);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendIfSupported(player, SyncConfigPayload.from(ModConfig.snapshot()));
        }
    }

    private static void broadcastConfigUpdate(ConfigSnapshot snapshot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
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
        if (player.connection.hasChannel(SyncConfigPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        NoCropTrampleCommand.register(event.getDispatcher());
    }

    private static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (TrampleRules.shouldPreventTrampling(
                event.getEntity(),
                event.getLevel(),
                event.getPos(),
                event.getFallDistance())) {
            event.setCanceled(true);
        }
    }
}
