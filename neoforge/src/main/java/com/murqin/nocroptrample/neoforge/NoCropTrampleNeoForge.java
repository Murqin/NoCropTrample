package com.murqin.nocroptrample.neoforge;

import com.murqin.nocroptrample.ModGameRules;
import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.TrampleRules;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.GameRuleChangedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * NeoForge entrypoint. Delegates all shared logic to {@link NoCropTrample}.
 * <p>
 * Uses NeoForge's native {@link BlockEvent.FarmlandTrampleEvent} and
 * {@link GameRuleChangedEvent} instead of mixins, so this side stays unaffected
 * by internal method renames and compatible with other mods.
 * </p>
 */
@Mod(NoCropTrample.MOD_ID)
public class NoCropTrampleNeoForge {

    private static final Set<MinecraftServer> DIRTY_SERVERS =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    public NoCropTrampleNeoForge(IEventBus modBus) {
        NoCropTrample.init(FMLPaths.CONFIGDIR.get());

        modBus.addListener(NoCropTrampleNeoForge::onRegister);
        modBus.addListener(NoCropTrampleNeoForge::onRegisterPayloads);

        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onFarmlandTrample);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onGameRuleChanged);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onServerStopped);
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.GAME_RULE, helper ->
                ModGameRules.registerAll(helper::register));
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .optional()
                .playToClient(SyncConfigPayload.TYPE, SyncConfigPayload.STREAM_CODEC);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ConfigSnapshot snapshot = ModGameRules.snapshot(player.level().getGameRules());
            sendIfSupported(player, SyncConfigPayload.from(snapshot));
        }
    }

    private static void onGameRuleChanged(GameRuleChangedEvent event) {
        if (ModGameRules.isModRule(event.getGameRule()) && event.getServer() != null) {
            DIRTY_SERVERS.add(event.getServer());
        }
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (DIRTY_SERVERS.remove(server)) {
            broadcastConfigUpdate(server, ModGameRules.snapshot(server.getGameRules()));
        }
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        DIRTY_SERVERS.remove(event.getServer());
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
