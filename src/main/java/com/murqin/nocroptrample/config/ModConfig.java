package com.murqin.nocroptrample.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.murqin.nocroptrample.NoCropTrampleMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration manager for the NoCropTrample mod.
 * <p>
 * Handles loading, saving, and accessing mod configuration values.
 * Config file is stored as JSON in the Fabric config directory.
 * </p>
 */
public class ModConfig {
    public enum FeatherFallingMode {
        DISABLED,
        ANY_LEVEL,
        SCALED
    }

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ConfigData.class, (InstanceCreator<ConfigData>) type -> new ConfigData())
            .create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(NoCropTrampleMod.MOD_ID + ".json");

    // Config values - encapsulated with getters/setters
    private static boolean preventPlayerTrampling = true;
    private static boolean preventMobTrampling = true;
    private static boolean preventEmptyTrampling = true;
    private static FeatherFallingMode featherFallingMode = FeatherFallingMode.ANY_LEVEL;
    private static boolean protectWithLeatherBoots = true;
    private static boolean preventPetTrampling = true;
    private static boolean preventVillagerTrampling = true;
    private static boolean preventDehydration = false;
    private static boolean preventEmptyReversion = false;
    private static boolean enableParticles = true;
    private static boolean enableSound = true;
    private static boolean enableActionBarMessage = false;
    private static int actionBarCooldownSeconds = 3;

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

    public static FeatherFallingMode getFeatherFallingMode() {
        return featherFallingMode;
    }

    public static void setFeatherFallingMode(FeatherFallingMode value) {
        featherFallingMode = value != null ? value : FeatherFallingMode.ANY_LEVEL;
        save();
    }

    public static boolean isProtectWithLeatherBoots() {
        return protectWithLeatherBoots;
    }

    public static void setProtectWithLeatherBoots(boolean value) {
        protectWithLeatherBoots = value;
        save();
    }

    public static boolean isPreventPetTrampling() {
        return preventPetTrampling;
    }

    public static void setPreventPetTrampling(boolean value) {
        preventPetTrampling = value;
        save();
    }

    public static boolean isPreventVillagerTrampling() {
        return preventVillagerTrampling;
    }

    public static void setPreventVillagerTrampling(boolean value) {
        preventVillagerTrampling = value;
        save();
    }

    public static boolean isPreventDehydration() {
        return preventDehydration;
    }

    public static void setPreventDehydration(boolean value) {
        preventDehydration = value;
        save();
    }

    public static boolean isPreventEmptyReversion() {
        return preventEmptyReversion;
    }

    public static void setPreventEmptyReversion(boolean value) {
        preventEmptyReversion = value;
        save();
    }

    public static boolean isEnableParticles() {
        return enableParticles;
    }

    public static void setEnableParticles(boolean value) {
        enableParticles = value;
        save();
    }

    public static boolean isEnableSound() {
        return enableSound;
    }

    public static void setEnableSound(boolean value) {
        enableSound = value;
        save();
    }

    public static boolean isEnableActionBarMessage() {
        return enableActionBarMessage;
    }

    public static void setEnableActionBarMessage(boolean value) {
        enableActionBarMessage = value;
        save();
    }

    public static int getActionBarCooldownSeconds() {
        return actionBarCooldownSeconds;
    }

    public static void setActionBarCooldownSeconds(int value) {
        actionBarCooldownSeconds = value;
        save();
    }

    /**
     * Loads configuration from disk.
     * If the config file doesn't exist, creates a new one with default values.
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data != null) {
                    preventPlayerTrampling = data.preventPlayerTrampling;
                    preventMobTrampling = data.preventMobTrampling;
                    preventEmptyTrampling = data.preventEmptyTrampling;
                    featherFallingMode = data.featherFallingMode != null ? data.featherFallingMode : FeatherFallingMode.ANY_LEVEL;
                    protectWithLeatherBoots = data.protectWithLeatherBoots;
                    preventPetTrampling = data.preventPetTrampling;
                    preventVillagerTrampling = data.preventVillagerTrampling;
                    preventDehydration = data.preventDehydration;
                    preventEmptyReversion = data.preventEmptyReversion;
                    enableParticles = data.enableParticles;
                    enableSound = data.enableSound;
                    enableActionBarMessage = data.enableActionBarMessage;
                    actionBarCooldownSeconds = data.actionBarCooldownSeconds;
                }
                NoCropTrampleMod.LOGGER.info("Config loaded from {}", CONFIG_PATH);
            } catch (IOException e) {
                NoCropTrampleMod.LOGGER.error("Failed to load config, using defaults", e);
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
            data.featherFallingMode = featherFallingMode;
            data.protectWithLeatherBoots = protectWithLeatherBoots;
            data.preventPetTrampling = preventPetTrampling;
            data.preventVillagerTrampling = preventVillagerTrampling;
            data.preventDehydration = preventDehydration;
            data.preventEmptyReversion = preventEmptyReversion;
            data.enableParticles = enableParticles;
            data.enableSound = enableSound;
            data.enableActionBarMessage = enableActionBarMessage;
            data.actionBarCooldownSeconds = actionBarCooldownSeconds;

            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
            NoCropTrampleMod.LOGGER.info("Config saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            NoCropTrampleMod.LOGGER.error("Failed to save config", e);
        }
    }

    /**
     * Inner class for JSON serialization/deserialization.
     */
    private static class ConfigData {
        boolean preventEmptyTrampling = true;
        boolean preventPlayerTrampling = true;
        boolean preventMobTrampling = true;
        FeatherFallingMode featherFallingMode = FeatherFallingMode.ANY_LEVEL;
        boolean protectWithLeatherBoots = true;
        boolean preventPetTrampling = true;
        boolean preventVillagerTrampling = true;
        boolean preventDehydration = false;
        boolean preventEmptyReversion = false;
        boolean enableParticles = true;
        boolean enableSound = true;
        boolean enableActionBarMessage = false;
        int actionBarCooldownSeconds = 3;
    }
}
