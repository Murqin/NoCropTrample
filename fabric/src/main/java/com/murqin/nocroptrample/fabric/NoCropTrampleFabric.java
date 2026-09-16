package com.murqin.nocroptrample.fabric;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric entrypoint. Delegates all shared logic to {@link NoCropTrample}.
 */
public class NoCropTrampleFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NoCropTrample.init(FabricLoader.getInstance().getConfigDir());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                NoCropTrampleCommand.register(dispatcher));
    }
}
