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
            double maxSafeFallDistance) {
        return new ConfigSnapshot(preventEmpty, preventPlayer, preventMob, minFeatherFalling, maxSafeFallDistance);
    }

    @Test
    void emptyFarmlandFollowsEmptySettingOnly() {
        assertTrue(TrampleRules.shouldCancel(true, true, false, false, 1.0, 0,
                config(true, false, false, 0, 0.0)));
        assertTrue(TrampleRules.shouldCancel(true, false, false, false, 1.0, 0,
                config(true, false, false, 0, 0.0)));
        assertFalse(TrampleRules.shouldCancel(true, true, false, false, 1.0, 0,
                config(false, true, true, 0, 0.0)));
        assertFalse(TrampleRules.shouldCancel(true, false, false, false, 1.0, 0,
                config(false, true, true, 0, 0.0)));
    }

    @Test
    void playerOnCropFollowsPlayerSetting() {
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 1.0, 0,
                config(false, true, false, 0, 0.0)));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 1.0, 0,
                config(true, false, true, 0, 0.0)));
    }

    @Test
    void mobOnCropFollowsMobSetting() {
        assertTrue(TrampleRules.shouldCancel(false, false, false, false, 1.0, 0,
                config(false, false, true, 0, 0.0)));
        assertFalse(TrampleRules.shouldCancel(false, false, false, false, 1.0, 0,
                config(true, true, false, 0, 0.0)));
    }

    @Test
    void featherFallingRequirementAppliesToPlayersOnPlantedAndEmpty() {
        ConfigSnapshot requireFf2 = config(true, true, true, 2, 0.0);

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
        ConfigSnapshot max3 = config(true, true, true, 0, 3.0);

        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 2.5, 0, max3));
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 3.0, 0, max3));
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 3.0625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 3.31, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 3.5625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 4.0625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(false, false, false, false, 4.0625, 0, max3));
        assertFalse(TrampleRules.shouldCancel(true, true, false, false, 4.0625, 0, max3));

        ConfigSnapshot max1 = config(true, true, true, 0, 1.0);
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 1.0625, 0, max1));
        assertFalse(TrampleRules.shouldCancel(false, true, false, false, 1.25, 0, max1));

        // Unlimited (0.0) protects any fall height
        assertTrue(TrampleRules.shouldCancel(false, true, false, false, 100.0, 0, ConfigSnapshot.DEFAULTS));
    }

    @Test
    void entityTagsOverrideNormalAndFallRules() {
        ConfigSnapshot strictAllOff = config(false, false, false, 4, 1.0);

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
        ConfigSnapshot clamped = new ConfigSnapshot(true, false, true, 99, -5.0);
        assertEquals(4, clamped.minFeatherFallingLevel());
        assertEquals(0.0, clamped.maxSafeFallDistance());

        ConfigSnapshot negativeFf = new ConfigSnapshot(true, true, true, -3, Double.NaN);
        assertEquals(0, negativeFf.minFeatherFallingLevel());
        assertEquals(0.0, negativeFf.maxSafeFallDistance());

        ConfigSnapshot infiniteDist = new ConfigSnapshot(true, true, true, 2, Double.POSITIVE_INFINITY);
        assertEquals(2, infiniteDist.minFeatherFallingLevel());
        assertEquals(0.0, infiniteDist.maxSafeFallDistance());
    }

    @Test
    void modConfigHandlesUpgradeFromV16SanitizesInvalidJsonAndResets(@TempDir Path tempDir) throws IOException {
        List<ConfigSnapshot> notifications = new ArrayList<>();
        ModConfig.setChangeListener(notifications::add);

        // 1. Fresh directory writes defaults
        ModConfig.init(tempDir);
        ModConfig.load();
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
        ModConfig.load();
        assertFalse(ModConfig.isPreventEmptyTrampling());
        assertTrue(ModConfig.isPreventPlayerTrampling());
        assertFalse(ModConfig.isPreventMobTrampling());
        assertEquals(0, ModConfig.getMinFeatherFallingLevel());
        assertEquals(0.0, ModConfig.getMaxSafeFallDistance());

        // 3. Hand-edited out-of-range values get clamped on load
        Files.writeString(configFile, """
                {
                  "preventEmptyTrampling": true,
                  "preventPlayerTrampling": true,
                  "preventMobTrampling": true,
                  "minFeatherFallingLevel": 10,
                  "maxSafeFallDistance": -3.5
                }
                """);
        ModConfig.load();
        assertEquals(4, ModConfig.getMinFeatherFallingLevel());
        assertEquals(0.0, ModConfig.getMaxSafeFallDistance());

        // 4. Malformed JSON falls back to defaults instead of throwing
        ModConfig.setPreventPlayerTrampling(false);
        Files.writeString(configFile, "{ not valid json");
        ModConfig.load();
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());

        // 5. Mutate + reset persists defaults and notifies listener
        ModConfig.setMinFeatherFallingLevel(3);
        ModConfig.setMaxSafeFallDistance(4.5);
        assertEquals(3, ModConfig.getMinFeatherFallingLevel());
        assertEquals(4.5, ModConfig.getMaxSafeFallDistance());

        ModConfig.reset();
        assertEquals(ConfigSnapshot.DEFAULTS, ModConfig.snapshot());
        assertEquals(ConfigSnapshot.DEFAULTS, notifications.getLast());
    }
}
