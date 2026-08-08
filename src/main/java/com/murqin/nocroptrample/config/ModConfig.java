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
        ALWAYS,
        REQUIRE_FEATHER_FALLING,
        SCALED_BY_LEVEL
    }

    public enum ModPreset {
        VANILLA_PLUS,
        CASUAL,
        HARDCORE,
        CUSTOM
    }

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ConfigData.class, (InstanceCreator<ConfigData>) type -> new ConfigData())
            .create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(NoCropTrampleMod.MOD_ID + ".json");

    // Config values - encapsulated with getters/setters
    private static ModPreset activePreset = ModPreset.VANILLA_PLUS;
    private static boolean preventPlayerTrampling = true;
    private static boolean preventMobTrampling = false;
    private static boolean preventEmptyTrampling = false;
    private static FeatherFallingMode featherFallingMode = FeatherFallingMode.ALWAYS;
    private static boolean protectWithLeatherBoots = true;
    private static boolean preventPetTrampling = true;
    private static boolean preventVillagerTrampling = true;
    private static boolean preventDehydration = false;
    private static boolean preventEmptyReversion = false;
    private static boolean enableParticles = true;
    private static boolean enableSound = false;
    private static boolean enableActionBarMessage = false;
    private static int actionBarCooldownSeconds = 3;

    public static ModPreset getActivePreset() {
        return activePreset;
    }

    public static void setPreset(ModPreset preset) {
        activePreset = preset != null ? preset : ModPreset.VANILLA_PLUS;
        applyPreset(activePreset);
        save();
    }

    /**
     * Immutable snapshot of every preset-controlled setting.
     * Single source of truth for both {@link #applyPreset} and {@link #matchesPreset},
     * so a preset can never be updated in one place and forgotten in the other.
     */
    private record PresetValues(
            boolean preventPlayerTrampling,
            boolean preventMobTrampling,
            boolean preventEmptyTrampling,
            FeatherFallingMode featherFallingMode,
            boolean protectWithLeatherBoots,
            boolean preventPetTrampling,
            boolean preventVillagerTrampling,
            boolean preventDehydration,
            boolean preventEmptyReversion,
            boolean enableParticles,
            boolean enableSound,
            boolean enableActionBarMessage
    ) {
    }

    private static PresetValues presetValues(ModPreset preset) {
        return switch (preset) {
            case VANILLA_PLUS -> new PresetValues(
                    true, false, false,
                    FeatherFallingMode.ALWAYS, true,
                    true, true,
                    false, false,
                    true, false, false);
            case CASUAL -> new PresetValues(
                    true, true, true,
                    FeatherFallingMode.ALWAYS, true,
                    true, true,
                    true, true,
                    true, true, false);
            case HARDCORE -> new PresetValues(
                    true, false, false,
                    FeatherFallingMode.SCALED_BY_LEVEL, false,
                    false, false,
                    false, false,
                    true, true, false);
            case CUSTOM -> null;
        };
    }

    public static void applyPreset(ModPreset preset) {
        if (preset == null) {
            return;
        }
        PresetValues values = presetValues(preset);
        if (values == null) {
            // CUSTOM: keep current settings
            return;
        }
        preventPlayerTrampling = values.preventPlayerTrampling();
        preventMobTrampling = values.preventMobTrampling();
        preventEmptyTrampling = values.preventEmptyTrampling();
        featherFallingMode = values.featherFallingMode();
        protectWithLeatherBoots = values.protectWithLeatherBoots();
        preventPetTrampling = values.preventPetTrampling();
        preventVillagerTrampling = values.preventVillagerTrampling();
        preventDehydration = values.preventDehydration();
        preventEmptyReversion = values.preventEmptyReversion();
        enableParticles = values.enableParticles();
        enableSound = values.enableSound();
        enableActionBarMessage = values.enableActionBarMessage();
    }

    public static boolean matchesPreset(ModPreset preset) {
        if (preset == null || preset == ModPreset.CUSTOM) {
            return true;
        }
        PresetValues values = presetValues(preset);
        return preventPlayerTrampling == values.preventPlayerTrampling()
                && preventMobTrampling == values.preventMobTrampling()
                && preventEmptyTrampling == values.preventEmptyTrampling()
                && featherFallingMode == values.featherFallingMode()
                && protectWithLeatherBoots == values.protectWithLeatherBoots()
                && preventPetTrampling == values.preventPetTrampling()
                && preventVillagerTrampling == values.preventVillagerTrampling()
                && preventDehydration == values.preventDehydration()
                && preventEmptyReversion == values.preventEmptyReversion()
                && enableParticles == values.enableParticles()
                && enableSound == values.enableSound()
                && enableActionBarMessage == values.enableActionBarMessage();
    }

    private static void checkPresetMatch() {
        if (activePreset != ModPreset.CUSTOM && !matchesPreset(activePreset)) {
            activePreset = ModPreset.CUSTOM;
        }
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
        checkPresetMatch();
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
        checkPresetMatch();
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
        checkPresetMatch();
        save();
    }

    public static FeatherFallingMode getFeatherFallingMode() {
        return featherFallingMode;
    }

    public static void setFeatherFallingMode(FeatherFallingMode value) {
        featherFallingMode = value != null ? value : FeatherFallingMode.ALWAYS;
        checkPresetMatch();
        save();
    }

    public static boolean isProtectWithLeatherBoots() {
        return protectWithLeatherBoots;
    }

    public static void setProtectWithLeatherBoots(boolean value) {
        protectWithLeatherBoots = value;
        checkPresetMatch();
        save();
    }

    public static boolean isPreventPetTrampling() {
        return preventPetTrampling;
    }

    public static void setPreventPetTrampling(boolean value) {
        preventPetTrampling = value;
        checkPresetMatch();
        save();
    }

    public static boolean isPreventVillagerTrampling() {
        return preventVillagerTrampling;
    }

    public static void setPreventVillagerTrampling(boolean value) {
        preventVillagerTrampling = value;
        checkPresetMatch();
        save();
    }

    public static boolean isPreventDehydration() {
        return preventDehydration;
    }

    public static void setPreventDehydration(boolean value) {
        preventDehydration = value;
        checkPresetMatch();
        save();
    }

    public static boolean isPreventEmptyReversion() {
        return preventEmptyReversion;
    }

    public static void setPreventEmptyReversion(boolean value) {
        preventEmptyReversion = value;
        checkPresetMatch();
        save();
    }

    public static boolean isEnableParticles() {
        return enableParticles;
    }

    public static void setEnableParticles(boolean value) {
        enableParticles = value;
        checkPresetMatch();
        save();
    }

    public static boolean isEnableSound() {
        return enableSound;
    }

    public static void setEnableSound(boolean value) {
        enableSound = value;
        checkPresetMatch();
        save();
    }

    public static boolean isEnableActionBarMessage() {
        return enableActionBarMessage;
    }

    public static void setEnableActionBarMessage(boolean value) {
        enableActionBarMessage = value;
        checkPresetMatch();
        save();
    }

    public static int getActionBarCooldownSeconds() {
        return actionBarCooldownSeconds;
    }

    public static void setActionBarCooldownSeconds(int value) {
        actionBarCooldownSeconds = Math.clamp(value, 0, 60);
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
                    // Always read the individual fields first, regardless of what (or whether)
                    // activePreset says - older config versions (pre-preset) never wrote that
                    // field, and re-deriving it via applyPreset() before reading would silently
                    // overwrite the user's saved values with preset defaults.
                    preventPlayerTrampling = data.preventPlayerTrampling;
                    preventMobTrampling = data.preventMobTrampling;
                    preventEmptyTrampling = data.preventEmptyTrampling;
                    featherFallingMode = data.featherFallingMode != null ? data.featherFallingMode : FeatherFallingMode.ALWAYS;
                    protectWithLeatherBoots = data.protectWithLeatherBoots;
                    preventPetTrampling = data.preventPetTrampling;
                    preventVillagerTrampling = data.preventVillagerTrampling;
                    preventDehydration = data.preventDehydration;
                    preventEmptyReversion = data.preventEmptyReversion;
                    enableParticles = data.enableParticles;
                    enableSound = data.enableSound;
                    enableActionBarMessage = data.enableActionBarMessage;
                    actionBarCooldownSeconds = Math.clamp(data.actionBarCooldownSeconds, 0, 60);

                    activePreset = data.activePreset != null ? data.activePreset : ModPreset.VANILLA_PLUS;
                    checkPresetMatch();
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
            data.activePreset = activePreset;
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
        ModPreset activePreset = ModPreset.VANILLA_PLUS;
        boolean preventEmptyTrampling = false;
        boolean preventPlayerTrampling = true;
        boolean preventMobTrampling = false;
        FeatherFallingMode featherFallingMode = FeatherFallingMode.ALWAYS;
        boolean protectWithLeatherBoots = true;
        boolean preventPetTrampling = true;
        boolean preventVillagerTrampling = true;
        boolean preventDehydration = false;
        boolean preventEmptyReversion = false;
        boolean enableParticles = true;
        boolean enableSound = false;
        boolean enableActionBarMessage = false;
        int actionBarCooldownSeconds = 3;
    }
}

