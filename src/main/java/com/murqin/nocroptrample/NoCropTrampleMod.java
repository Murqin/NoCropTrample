package com.murqin.nocroptrample;

import com.murqin.nocroptrample.command.NoCropTrampleCommand;
import com.murqin.nocroptrample.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoCropTrampleMod implements ModInitializer {
    public static final String MOD_ID = "nocroptrample";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("NoCropTrample mod initialized!");
        
        // Load config
        ModConfig.load();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NoCropTrampleCommand.register(dispatcher)
        );

        LOGGER.info("Empty trampling prevention: {}", ModConfig.preventEmptyTrampling ? "ON" : "OFF");
        LOGGER.info("Player trampling prevention: {}", ModConfig.preventPlayerTrampling ? "ON" : "OFF");
        LOGGER.info("Mob trampling prevention: {}", ModConfig.preventMobTrampling ? "ON" : "OFF");
    }
}
