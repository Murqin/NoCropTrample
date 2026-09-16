package com.murqin.nocroptrample.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonParseException;
import com.murqin.nocroptrample.NoCropTrample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration manager for the NoCropTrample mod.
 * <p>
 * Handles loading, saving, and accessing mod configuration values.
 * Config file is stored as JSON in the platform's config directory.
 * </p>
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ConfigData.class, (InstanceCreator<ConfigData>) type -> new ConfigData())
            .create();

    private static Path configPath;

    // Config values - encapsulated with getters/setters
    private static boolean preventPlayerTrampling = true;
    private static boolean preventMobTrampling = true;
    private static boolean preventEmptyTrampling = true;

    /**
     * Sets the directory the config file lives in. Must be called once by
     * the platform entrypoint, before {@link #load()}.
     *
     * @param configDir the platform's config directory
     */
    public static void init(Path configDir) {
        configPath = configDir.resolve(NoCropTrample.MOD_ID + ".json");
    }

    /**
     * Gets whether empty trampling prevention is enabled.
     *
     * @return true if empty farmland cannot be trampled, false otherwise
     */
    public static boolean isPreventEmptyTrampling() {
        return preventEmptyTrampling;
    }

    /**
     * Sets whether empty trampling prevention is enabled.
     * Automatically saves the configuration after updating.
     *
     * @param value true to prevent trampling of empty farmland, false otherwise
     */
    public static void setPreventEmptyTrampling(boolean value) {
        preventEmptyTrampling = value;
        save();
    }


    /**
     * Gets whether player trampling prevention is enabled.
     *
     * @return true if players cannot trample farmland, false otherwise
     */
    public static boolean isPreventPlayerTrampling() {
        return preventPlayerTrampling;
    }

    /**
     * Sets whether player trampling prevention is enabled.
     * Automatically saves the configuration after updating.
     *
     * @param value true to prevent players from trampling farmland, false otherwise
     */
    public static void setPreventPlayerTrampling(boolean value) {
        preventPlayerTrampling = value;
        save();
    }

    /**
     * Gets whether mob trampling prevention is enabled.
     *
     * @return true if mobs cannot trample farmland, false otherwise
     */
    public static boolean isPreventMobTrampling() {
        return preventMobTrampling;
    }

    /**
     * Sets whether mob trampling prevention is enabled.
     * Automatically saves the configuration after updating.
     *
     * @param value true to prevent mobs from trampling farmland, false otherwise
     */
    public static void setPreventMobTrampling(boolean value) {
        preventMobTrampling = value;
        save();
    }

    /**
     * Loads configuration from disk.
     * If the config file doesn't exist, creates a new one with default values.
     */
    public static void load() {
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data != null) {
                    preventPlayerTrampling = data.preventPlayerTrampling;
                    preventMobTrampling = data.preventMobTrampling;
                    preventEmptyTrampling = data.preventEmptyTrampling;
                }
                NoCropTrample.LOGGER.info("Config loaded from {}", configPath);
            } catch (IOException | JsonParseException e) {
                NoCropTrample.LOGGER.error("Failed to load config from {}, falling back to defaults", configPath, e);
                preventPlayerTrampling = true;
                preventMobTrampling = true;
                preventEmptyTrampling = true;
            }
        } else {
            save(); // Create default config
        }
    }

    /**
     * Saves the current configuration to disk.
     */
    public static void save() {
        try {
            ConfigData data = new ConfigData();
            data.preventPlayerTrampling = preventPlayerTrampling;
            data.preventMobTrampling = preventMobTrampling;
            data.preventEmptyTrampling = preventEmptyTrampling;

            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(data));
            NoCropTrample.LOGGER.info("Config saved to {}", configPath);
        } catch (IOException e) {
            NoCropTrample.LOGGER.error("Failed to save config", e);
        }
    }

    /**
     * Inner class for JSON serialization/deserialization.
     */
    private static class ConfigData {
        boolean preventEmptyTrampling = true;
        boolean preventPlayerTrampling = true;
        boolean preventMobTrampling = true;
    }
}
