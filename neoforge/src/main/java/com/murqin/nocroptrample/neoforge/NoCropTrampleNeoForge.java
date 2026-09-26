package com.murqin.nocroptrample.neoforge;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.TrampleRules;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

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

    public NoCropTrampleNeoForge(IEventBus modBus, ModContainer container) {
        NoCropTrample.init(FMLPaths.CONFIGDIR.get());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NoCropTrampleNeoForgeClient.init(container);
        }

        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(NoCropTrampleNeoForge::onFarmlandTrample);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        NoCropTrampleCommand.register(event.getDispatcher());
    }

    private static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (TrampleRules.shouldPreventTrampling(event.getEntity(), event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }
}
