package com.murqin.nocroptrample.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonParseException;
import com.murqin.nocroptrample.NoCropTrample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

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
    private static volatile Consumer<ConfigSnapshot> changeListener = snapshot -> {};

    // Config values - encapsulated with getters/setters
    private static boolean preventEmptyTrampling = true;
    private static boolean preventPlayerTrampling = true;
    private static boolean preventMobTrampling = true;
    private static int minFeatherFallingLevel = 0;
    private static double maxSafeFallDistance = 0.0;

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
     * Registers a listener invoked whenever configuration changes via
     * {@link #load()}, {@link #save()}, or {@link #reset()}.
     *
     * @param listener consumer receiving the updated {@link ConfigSnapshot}, or {@code null} to clear
     */
    public static void setChangeListener(Consumer<ConfigSnapshot> listener) {
        changeListener = listener != null ? listener : snapshot -> {};
    }

    /**
     * Returns an immutable snapshot of all current configuration values.
     */
    public static ConfigSnapshot snapshot() {
        return new ConfigSnapshot(
                preventEmptyTrampling,
                preventPlayerTrampling,
                preventMobTrampling,
                minFeatherFallingLevel,
                maxSafeFallDistance);
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
     * Gets the minimum Feather Falling enchantment level required for players
     * to avoid trampling farmland ({@code 0} means no enchantment required).
     */
    public static int getMinFeatherFallingLevel() {
        return minFeatherFallingLevel;
    }

    /**
     * Sets the minimum Feather Falling level (clamped to {@code 0..4}) and saves.
     */
    public static void setMinFeatherFallingLevel(int level) {
        minFeatherFallingLevel = ConfigSnapshot.clampFeatherFallingLevel(level);
        save();
    }

    /**
     * Gets the maximum fall distance in blocks that remains protected
     * ({@code 0.0} means unlimited protection).
     */
    public static double getMaxSafeFallDistance() {
        return maxSafeFallDistance;
    }

    /**
     * Sets the maximum protected fall distance in blocks ({@code <= 0} or non-finite becomes {@code 0.0})
     * and saves.
     */
    public static void setMaxSafeFallDistance(double distance) {
        maxSafeFallDistance = ConfigSnapshot.sanitizeFallDistance(distance);
        save();
    }

    /**
     * Resets all settings to {@link ConfigSnapshot#DEFAULTS} and saves to disk.
     */
    public static void reset() {
        applySnapshot(ConfigSnapshot.DEFAULTS);
        save();
    }

    /**
     * Loads configuration from disk.
     * If the config file doesn't exist, creates a new one with default values.
     */
    public static void load() {
        if (Files.exists(configPath)) {
            ConfigSnapshot loaded;
            try {
                String json = Files.readString(configPath);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                loaded = (data != null)
                        ? new ConfigSnapshot(
                                data.preventEmptyTrampling,
                                data.preventPlayerTrampling,
                                data.preventMobTrampling,
                                data.minFeatherFallingLevel,
                                data.maxSafeFallDistance)
                        : ConfigSnapshot.DEFAULTS;
                NoCropTrample.LOGGER.info("Config loaded from {}", configPath);
            } catch (IOException | JsonParseException e) {
                NoCropTrample.LOGGER.error("Failed to load config from {}, falling back to defaults", configPath, e);
                loaded = ConfigSnapshot.DEFAULTS;
            }
            applySnapshot(loaded);
            changeListener.accept(loaded);
        } else {
            applySnapshot(ConfigSnapshot.DEFAULTS);
            save(); // Create default config
        }
    }

    /**
     * Saves the current configuration to disk.
     */
    public static void save() {
        ConfigSnapshot current = snapshot();
        try {
            ConfigData data = new ConfigData();
            data.preventEmptyTrampling = current.preventEmptyTrampling();
            data.preventPlayerTrampling = current.preventPlayerTrampling();
            data.preventMobTrampling = current.preventMobTrampling();
            data.minFeatherFallingLevel = current.minFeatherFallingLevel();
            data.maxSafeFallDistance = current.maxSafeFallDistance();

            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(data));
            NoCropTrample.LOGGER.info("Config saved to {}", configPath);
        } catch (IOException e) {
            NoCropTrample.LOGGER.error("Failed to save config", e);
        }
        changeListener.accept(current);
    }

    private static void applySnapshot(ConfigSnapshot snapshot) {
        preventEmptyTrampling = snapshot.preventEmptyTrampling();
        preventPlayerTrampling = snapshot.preventPlayerTrampling();
        preventMobTrampling = snapshot.preventMobTrampling();
        minFeatherFallingLevel = snapshot.minFeatherFallingLevel();
        maxSafeFallDistance = snapshot.maxSafeFallDistance();
    }

    /**
     * Inner class for JSON serialization/deserialization.
     */
    private static class ConfigData {
        boolean preventEmptyTrampling = true;
        boolean preventPlayerTrampling = true;
        boolean preventMobTrampling = true;
        int minFeatherFallingLevel = 0;
        double maxSafeFallDistance = 0.0;
    }
}
