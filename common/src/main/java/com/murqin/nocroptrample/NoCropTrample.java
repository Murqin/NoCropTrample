package com.murqin.nocroptrample;

import com.murqin.nocroptrample.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Shared mod bootstrap, called by each platform's entrypoint.
 */
public final class NoCropTrample {
    public static final String MOD_ID = "nocroptrample";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private NoCropTrample() {
    }

    /**
     * Loads the config and logs the active settings. Must be called once by
     * the platform entrypoint, before anything reads {@link ModConfig}.
     *
     * @param configDir the platform's config directory
     */
    public static void init(Path configDir) {
        LOGGER.info("NoCropTrample mod initialized!");

        ModConfig.init(configDir);
        ModConfig.load();

        LOGGER.info("Empty trampling prevention: {}", ModConfig.isPreventEmptyTrampling() ? "ON" : "OFF");
        LOGGER.info("Player trampling prevention: {}", ModConfig.isPreventPlayerTrampling() ? "ON" : "OFF");
        LOGGER.info("Mob trampling prevention: {}", ModConfig.isPreventMobTrampling() ? "ON" : "OFF");
    }
}
