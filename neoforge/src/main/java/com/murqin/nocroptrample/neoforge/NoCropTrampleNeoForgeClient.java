package com.murqin.nocroptrample.neoforge;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.client.ClientConfigState;
import com.murqin.nocroptrample.client.CreateWorldDefaultsHelper;
import com.murqin.nocroptrample.client.NoCropTrampleConfigScreen;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.network.SyncConfigPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only NeoForge entrypoint, wiring the shared config screen into the
 * Mods menu, receiving optional server config sync, and seeding newly opened
 * Create World screens with updated defaults. Never loaded on a dedicated server.
 */
@Mod(value = NoCropTrample.MOD_ID, dist = Dist.CLIENT)
public class NoCropTrampleNeoForgeClient {

    public NoCropTrampleNeoForgeClient(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (screenContainer, parent) -> new NoCropTrampleConfigScreen(parent));

        modBus.addListener((RegisterClientPayloadHandlersEvent event) ->
                event.register(SyncConfigPayload.TYPE, (payload, context) -> {
                    long generation = ClientConfigState.currentGeneration();
                    ConfigSnapshot snapshot = payload.toSnapshot();
                    context.enqueueWork(() -> {
                        if (Minecraft.getInstance().getConnection() != null) {
                            ClientConfigState.setServerConfigIfGeneration(snapshot, generation);
                        }
                    });
                }));

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientConfigState.clear());
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Pre event) ->
                CreateWorldDefaultsHelper.applyIfCreateWorldScreen(event.getScreen()));
    }
}
