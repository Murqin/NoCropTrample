package com.murqin.nocroptrample;

import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Compiled into both platform builds, so each one checks the rules against its
// own Minecraft classpath.
class TrampleRulesTest {

    @AfterEach
    void clearConfigListener() {
        ModConfig.setChangeListener(null);
    }

    private static ConfigSnapshot config(
            boolean preventEmpty,
            boolean preventPlayer,
            boolean preventMob,
            int minFeatherFalling,
            int maxSafeFallDistance) {
        return new ConfigSnapshot(preventEmpty, preventPlayer, preventMob, minFeatherFalling, maxSafeFallDistance);
    }

    @Test
    void emptyFarmlandFollowsEmptySettingOnly() {
        assertTrue(TrampleRules.shouldCancel(true, true, false, false, 1.0, 0,
                config(true, false, false, 0, 0)));
        assertTrue(TrampleRules.shouldCancel(true, false, false, false, 1.0, 0,
                config(true, false, false, 0, 0)));
        assertFalse(TrampleRules.shouldCancel(true, true, false, false, 1.0, 0,
                config(false, true, true, 0, 0)));
        assertFalse(TrampleRules.shouldCancel(true, false, false, false, 1.0, 0,
                config(false, true, true, 0, 0)));
    }

    @Test
    void playerOnCropFollowsPlayerSetting() {
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 1.0, 0,
                config(false, true, false, 0, 0)));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 1.0, 0,
                config(true, false, true, 0, 0)));
    }

    @Test
    void mobOnCropFollowsMobSetting() {
        assertTrue(TrampleRules.shouldCancel(false, false, false, false, 1.0, 0,
                config(false, false, true, 0, 0)));
        assertFalse(TrampleRules.shouldCancel(false, false, false, false, 1.0, 0,
                config(true, true, false, 0, 0)));
    }

    @Test
    void featherFallingRequirementAppliesToPlayersOnPlantedAndEmpty() {
        ConfigSnapshot requireFf2 = config(true, true, true, 2, 0);

        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 1.0, 0, requireFf2));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 1.0, 1, requireFf2));
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 1.0, 2, requireFf2));
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 1.0, 4, requireFf2));

        // Empty farmland with player also requires Feather Falling when empty=true and minFF > 0
        assertFalse(TrampleRules.shouldCancel(true, true, false, false, 1.0, 1, requireFf2));
        assertTrue(TrampleRules.shouldCancel(true, true, false, false, 1.0, 2, requireFf2));

        // Mobs are unaffected by Feather Falling requirement
        assertTrue(TrampleRules.shouldCancel(false, false, false, false, 1.0, 0, requireFf2));
        assertTrue(TrampleRules.shouldCancel(true, false, false, false, 1.0, 0, requireFf2));
    }

    @Test
    void maxSafeFallDistanceAllowsHighFalls() {
        ConfigSnapshot max3 = config(true, true, true, 0, 3);

        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 2.5, 0, max3));
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 3.0, 0, max3));
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 3.0625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 3.31, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 3.5625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 4.0625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, false, false, false, 4.0625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(true, true, false, false, 4.0625, 0, max3));

        ConfigSnapshot max1 = config(true, true, true, 0, 1);
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 1.0625, 0, max1));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 1.25, 0, max1));

        // Unlimited (0) protects any fall height
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 100.0, 0, ConfigSnapshot.DEFAULTS));
    }

    @Test
    void entityTagsOverrideNormalAndFallRules() {
        ConfigSnapshot strictAllOff = config(false, false, false, 4, 1);

        // Exempt protects even when rules are off or fall distance exceeds threshold
        assertTrue(TrampleRules.shouldCancel(false, false, false, true, 10.0, 0, strictAllOff));
        assertTrue(TrampleRules.shouldCancel(true, false, false, true, 10.0, 0, strictAllOff));
        assertTrue(TrampleRules.shouldCancel(false, true, false, true, 10.0, 0, strictAllOff));

        // Always trample wins over all protections including exempt
        assertFalse(TrampleRules.shouldCancel(false, false, true, true, 0.5, 4, ConfigSnapshot.DEFAULTS));
        assertFalse(TrampleRules.shouldCancel(false, true, true, false, 0.5, 4, ConfigSnapshot.DEFAULTS));
        assertFalse(TrampleRules.shouldCancel(true, false, true, false, 0.5, 0, ConfigSnapshot.DEFAULTS));
    }

    @Test
    void configSnapshotClampsAndSanitizesOutOfRangeValues() {
        ConfigSnapshot clamped = new ConfigSnapshot(true, false, true, 99, 500);
        assertEquals(4, clamped.minFeatherFallingLevel());
        assertEquals(100, clamped.maxSafeFallDistance());

        ConfigSnapshot negative = new ConfigSnapshot(true, true, true, -3, -5);
        assertEquals(0, negative.minFeatherFallingLevel());
        assertEquals(0, negative.maxSafeFallDistance());

        assertEquals(0, ConfigSnapshot.sanitizeFallDistance(Double.NaN));
        assertEquals(0, ConfigSnapshot.sanitizeFallDistance(Double.POSITIVE_INFINITY));
        assertEquals(0, ConfigSnapshot.sanitizeFallDistance(-3.5));
        assertEquals(4, ConfigSnapshot.sanitizeFallDistance(3.5));
        assertEquals(3, ConfigSnapshot.sanitizeFallDistance(3.0));
        assertEquals(100, ConfigSnapshot.sanitizeFallDistance(250.0));
        assertEquals(100, ConfigSnapshot.sanitizeFallDistance(999_999_999_999.0));
        assertEquals(100, ConfigSnapshot.sanitizeFallDistance(1e20));
        assertEquals(100, ConfigSnapshot.sanitizeFallDistance(4_294_967_301.0));
    }

    @Test
    void modConfigHandlesUpgradeFromV16LegacyFloatsInvalidJsonAndResets(@TempDir Path tempDir) throws IOException {
        List<ConfigSnapshot> notifications = new ArrayList<>();
        ModConfig.setChangeListener(notifications::add);

        // 1. Fresh directory writes defaults
        ModConfig.init(tempDir);
        assertTrue(ModConfig.load());
        assertTrue(Files.exists(tempDir.resolve("nocroptrample.json")));
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());
        assertEquals(1, notifications.size());

        // 2. Legacy 1.6 config file missing new fields retains defaults for new fields
        Path configFile = tempDir.resolve("nocroptrample.json");
        Files.writeString(configFile, """
                {
                  "preventEmptyTrampling": false,
                  "preventPlayerTrampling": true,
                  "preventMobTrampling": false
                }
                """);
        assertTrue(ModConfig.load());
        assertFalse(ModConfig.isPreventEmptyTrampling());
        assertTrue(ModConfig.isPreventPlayerTrampling());
        assertFalse(ModConfig.isPreventMobTrampling());
        assertEquals(0, ModConfig.getMinFeatherFallingLevel());
        assertEquals(0, ModConfig.getMaxSafeFallDistance());

        // 3. Legacy decimal fall distance and out-of-range values sanitize cleanly on load
        Files.writeString(configFile, """
                {
                  "preventEmptyTrampling": true,
                  "preventPlayerTrampling": true,
                  "preventMobTrampling": true,
                  "minFeatherFallingLevel": 10,
                  "maxSafeFallDistance": 3.5
                }
                """);
        assertTrue(ModConfig.load());
        assertEquals(4, ModConfig.getMinFeatherFallingLevel());
        assertEquals(4, ModConfig.getMaxSafeFallDistance());

        Files.writeString(configFile, """
                {
                  "preventEmptyTrampling": true,
                  "preventPlayerTrampling": true,
                  "preventMobTrampling": true,
                  "minFeatherFallingLevel": -2,
                  "maxSafeFallDistance": -3.5
                }
                """);
        assertTrue(ModConfig.load());
        assertEquals(0, ModConfig.getMinFeatherFallingLevel());
        assertEquals(0, ModConfig.getMaxSafeFallDistance());

        // 4. Non-numeric string maxSafeFallDistance falls back safely without NumberFormatException
        Files.writeString(configFile, """
                {
                  "preventEmptyTrampling": false,
                  "maxSafeFallDistance": "unlimited"
                }
                """);
        assertFalse(ModConfig.load());
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());

        // 5. Empty file falls back safely on startup load
        Files.writeString(configFile, "");
        assertFalse(ModConfig.load());
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());

        // 6. Malformed JSON falls back to defaults on load() instead of throwing
        ModConfig.setPreventPlayerTrampling(false);
        Files.writeString(configFile, "{ not valid json");
        assertFalse(ModConfig.load());
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());

        // 7. reload() preserves current snapshot on malformed, string-typed, and empty files
        ModConfig.applyAndSave(new ConfigSnapshot(false, true, false, 2, 12));
        ConfigSnapshot preserved = ModConfig.snapshot();

        Files.writeString(configFile, "{ not valid json");
        assertFalse(ModConfig.reload());
        assertEquals(preserved, ModConfig.snapshot());

        Files.writeString(configFile, "{\"maxSafeFallDistance\": \"unlimited\"}");
        assertFalse(ModConfig.reload());
        assertEquals(preserved, ModConfig.snapshot());

        Files.writeString(configFile, "");
        assertFalse(ModConfig.reload());
        assertEquals(preserved, ModConfig.snapshot());

        // 8. No-op sets / applyAndSave / reset skip disk writes and listener calls
        int countBeforeNoOps = notifications.size();
        ModConfig.applyAndSave(preserved);
        ModConfig.setMinFeatherFallingLevel(2);
        ModConfig.setMaxSafeFallDistance(12);
        assertEquals(countBeforeNoOps, notifications.size());

        // 9. Mutate + reset persists defaults and no-op repeat reset skips rewrite
        ModConfig.setMinFeatherFallingLevel(3);
        ModConfig.setMaxSafeFallDistance(5);
        assertEquals(3, ModConfig.getMinFeatherFallingLevel());
        assertEquals(5, ModConfig.getMaxSafeFallDistance());

        ModConfig.reset();
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());
        assertEquals(ConfigSnapshot.DEFAULTS, notifications.getLast());

        int countAfterReset = notifications.size();
        ModConfig.reset();
        assertEquals(countAfterReset, notifications.size());

        // 10. Atomic save leaves no temporary files behind
        try (Stream<Path> files = Files.list(tempDir)) {
            assertFalse(files.anyMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }

        // 11. Uninitialized path handles load/save gracefully
        ModConfig.init(null);
        assertFalse(ModConfig.load());
        ModConfig.save();
    }
}
