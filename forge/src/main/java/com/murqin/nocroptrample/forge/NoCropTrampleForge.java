package com.murqin.nocroptrample.forge;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.TrampleRules;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Forge entrypoint. Delegates all shared logic to {@link NoCropTrample}.
 * <p>
 * Uses Forge's native {@link BlockEvent.FarmlandTrampleEvent} instead of a
 * mixin, so this side isn't affected by Mojang renaming FarmlandBlock's
 * internal methods, and stays compatible with other mods listening on the
 * same event.
 * </p>
 */
@Mod(NoCropTrample.MOD_ID)
public class NoCropTrampleForge {

    public NoCropTrampleForge() {
        NoCropTrample.init(FMLPaths.CONFIGDIR.get());

        MinecraftForge.EVENT_BUS.addListener(NoCropTrampleForge::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(NoCropTrampleForge::onFarmlandTrample);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NoCropTrampleForgeClient.init();
        }
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
