package com.murqin.nocroptrample.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.murqin.nocroptrample.NoCropTrample;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/**
 * Configuration manager for NoCropTrample default settings ({@code config/nocroptrample.json}).
 * <p>
 * Active worlds store their live settings per-world in Minecraft's GameRules;
 * this config provides global defaults for new worlds and {@code /nocroptrample reload}.
 * </p>
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ConfigData.class, (InstanceCreator<ConfigData>) type -> new ConfigData())
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) ->
                    src != null && Double.isFinite(src) && src == Math.rint(src)
                            ? new JsonPrimitive(src.longValue())
                            : new JsonPrimitive(src))
            .create();

    private static final Object IO_LOCK = new Object();

    private static volatile Path configPath;
    private static volatile Consumer<ConfigSnapshot> changeListener = snapshot -> {};
    private static volatile ConfigSnapshot current = ConfigSnapshot.DEFAULTS;

    /**
     * Sets the directory the config file lives in. Must be called once by
     * the platform entrypoint, before {@link #load()}.
     *
     * @param configDir the platform's config directory
     */
    public static void init(Path configDir) {
        configPath = configDir != null
                ? configDir.toAbsolutePath().resolve(NoCropTrample.MOD_ID + ".json")
                : null;
    }

    /**
     * Registers a listener invoked whenever configuration changes via
     * {@link #load()}, {@link #reload()}, {@link #save()}, {@link #applyAndSave(ConfigSnapshot)},
     * or {@link #reset()}.
     *
     * @param listener consumer receiving the updated {@link ConfigSnapshot}, or {@code null} to clear
     */
    public static void setChangeListener(Consumer<ConfigSnapshot> listener) {
        changeListener = listener != null ? listener : snapshot -> {};
    }

    /**
     * Returns an immutable snapshot of all current default configuration values.
     */
    public static ConfigSnapshot snapshot() {
        return current;
    }

    public static boolean isPreventEmptyTrampling() {
        return current.preventEmptyTrampling();
    }

    public static void setPreventEmptyTrampling(boolean value) {
        synchronized (IO_LOCK) {
            ConfigSnapshot c = current;
            applyAndSave(new ConfigSnapshot(
                    value,
                    c.preventPlayerTrampling(),
                    c.preventMobTrampling(),
                    c.minFeatherFallingLevel(),
                    c.maxSafeFallDistance()));
        }
    }

    public static boolean isPreventPlayerTrampling() {
        return current.preventPlayerTrampling();
    }

    public static void setPreventPlayerTrampling(boolean value) {
        synchronized (IO_LOCK) {
            ConfigSnapshot c = current;
            applyAndSave(new ConfigSnapshot(
                    c.preventEmptyTrampling(),
                    value,
                    c.preventMobTrampling(),
                    c.minFeatherFallingLevel(),
                    c.maxSafeFallDistance()));
        }
    }

    public static boolean isPreventMobTrampling() {
        return current.preventMobTrampling();
    }

    public static void setPreventMobTrampling(boolean value) {
        synchronized (IO_LOCK) {
            ConfigSnapshot c = current;
            applyAndSave(new ConfigSnapshot(
                    c.preventEmptyTrampling(),
                    c.preventPlayerTrampling(),
                    value,
                    c.minFeatherFallingLevel(),
                    c.maxSafeFallDistance()));
        }
    }

    public static int getMinFeatherFallingLevel() {
        return current.minFeatherFallingLevel();
    }

    public static void setMinFeatherFallingLevel(int level) {
        synchronized (IO_LOCK) {
            ConfigSnapshot c = current;
            applyAndSave(new ConfigSnapshot(
                    c.preventEmptyTrampling(),
                    c.preventPlayerTrampling(),
                    c.preventMobTrampling(),
                    ConfigSnapshot.clampFeatherFallingLevel(level),
                    c.maxSafeFallDistance()));
        }
    }

    public static int getMaxSafeFallDistance() {
        return current.maxSafeFallDistance();
    }

    public static void setMaxSafeFallDistance(int distance) {
        synchronized (IO_LOCK) {
            ConfigSnapshot c = current;
            applyAndSave(new ConfigSnapshot(
                    c.preventEmptyTrampling(),
                    c.preventPlayerTrampling(),
                    c.preventMobTrampling(),
                    c.minFeatherFallingLevel(),
                    ConfigSnapshot.clampFallDistance(distance)));
        }
    }

    /**
     * Applies a full snapshot and atomically writes it to disk only if it differs from current.
     *
     * @param updated the new configuration snapshot
     */
    public static void applyAndSave(ConfigSnapshot updated) {
        if (updated == null) {
            return;
        }
        synchronized (IO_LOCK) {
            if (updated.equals(current)) {
                return;
            }
            current = updated;
            saveLocked(updated);
        }
    }

    /**
     * Resets all default settings to {@link ConfigSnapshot#DEFAULTS} and saves to disk if changed.
     */
    public static void reset() {
        applyAndSave(ConfigSnapshot.DEFAULTS);
    }

    /**
     * Startup configuration load. Creates defaults if missing; falls back to
     * {@link ConfigSnapshot#DEFAULTS} if malformed or unreadable.
     *
     * @return {@code true} if config was loaded or created cleanly, {@code false} on failure fallback
     */
    public static boolean load() {
        return loadInternal(false);
    }

    /**
     * Runtime configuration reload (e.g., {@code /nocroptrample reload}).
     * Preserves the active in-memory defaults if the file on disk is malformed or unreadable.
     *
     * @return {@code true} if reloaded cleanly, {@code false} if failed and previous values were kept
     */
    public static boolean reload() {
        return loadInternal(true);
    }

    private static boolean loadInternal(boolean preserveOnFailure) {
        synchronized (IO_LOCK) {
            Path path = configPath;
            if (path == null) {
                NoCropTrample.LOGGER.warn("Config path not initialized; skipping load");
                return false;
            }
            if (!Files.exists(path)) {
                current = ConfigSnapshot.DEFAULTS;
                saveLocked(current);
                return true;
            }

            ConfigSnapshot loaded;
            try {
                String json = Files.readString(path);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data == null) {
                    NoCropTrample.LOGGER.warn("Config file {} is empty or null", path);
                    if (preserveOnFailure) {
                        return false;
                    }
                    current = ConfigSnapshot.DEFAULTS;
                    changeListener.accept(current);
                    return false;
                }
                int fallDistance = data.maxSafeFallDistance != null
                        ? ConfigSnapshot.sanitizeFallDistance(data.maxSafeFallDistance)
                        : 0;
                loaded = new ConfigSnapshot(
                        data.preventEmptyTrampling,
                        data.preventPlayerTrampling,
                        data.preventMobTrampling,
                        data.minFeatherFallingLevel,
                        fallDistance);
                NoCropTrample.LOGGER.info("Config loaded from {}", path);
            } catch (IOException | JsonParseException | NumberFormatException e) {
                NoCropTrample.LOGGER.error("Failed to load config from {}", path, e);
                if (preserveOnFailure) {
                    return false;
                }
                current = ConfigSnapshot.DEFAULTS;
                changeListener.accept(current);
                return false;
            }
            current = loaded;
            changeListener.accept(loaded);
            return true;
        }
    }

    /**
     * Saves the current configuration atomically to disk.
     */
    public static void save() {
        synchronized (IO_LOCK) {
            saveLocked(current);
        }
    }

    private static void saveLocked(ConfigSnapshot snapshot) {
        Path path = configPath;
        if (path == null) {
            NoCropTrample.LOGGER.warn("Config path not initialized; skipping save");
            return;
        }

        Path tmp = null;
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            ConfigData data = new ConfigData();
            data.preventEmptyTrampling = snapshot.preventEmptyTrampling();
            data.preventPlayerTrampling = snapshot.preventPlayerTrampling();
            data.preventMobTrampling = snapshot.preventMobTrampling();
            data.minFeatherFallingLevel = snapshot.minFeatherFallingLevel();
            data.maxSafeFallDistance = (double) snapshot.maxSafeFallDistance();

            Path tempDir = parent != null ? parent : Path.of(".");
            tmp = Files.createTempFile(tempDir, NoCropTrample.MOD_ID + "-", ".tmp");
            Files.writeString(tmp, GSON.toJson(data));
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            NoCropTrample.LOGGER.info("Config saved to {}", path);
        } catch (IOException e) {
            NoCropTrample.LOGGER.error("Failed to save config", e);
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                }
            }
        }
        changeListener.accept(snapshot);
    }

    private static class ConfigData {
        boolean preventEmptyTrampling = true;
        boolean preventPlayerTrampling = true;
        boolean preventMobTrampling = true;
        int minFeatherFallingLevel = 0;
        Double maxSafeFallDistance = 0.0;
    }
}
