package com.murqin.nocroptrample.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.murqin.nocroptrample.NoCropTrampleMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(NoCropTrampleMod.MOD_ID + ".json");

    // Config values
    public static boolean preventPlayerTrampling = true;
    public static boolean preventMobTrampling = true;
    public static boolean preventEmptyTrampling = true;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data != null) {
                    preventPlayerTrampling = data.preventPlayerTrampling;
                    preventMobTrampling = data.preventMobTrampling;
                    preventEmptyTrampling = data.preventEmptyTrampling;
                }
                NoCropTrampleMod.LOGGER.info("Config loaded from {}", CONFIG_PATH);
            } catch (IOException e) {
                NoCropTrampleMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            save(); // Create default config
        }
    }

    public static void save() {
        try {
            ConfigData data = new ConfigData();
            data.preventPlayerTrampling = preventPlayerTrampling;
            data.preventMobTrampling = preventMobTrampling;
            data.preventEmptyTrampling = preventEmptyTrampling;
            
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
            NoCropTrampleMod.LOGGER.info("Config saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            NoCropTrampleMod.LOGGER.error("Failed to save config", e);
        }
    }

    // Inner class for JSON serialization
    private static class ConfigData {
        boolean preventEmptyTrampling = true;
        boolean preventPlayerTrampling = true;
        boolean preventMobTrampling = true;
    }
}
