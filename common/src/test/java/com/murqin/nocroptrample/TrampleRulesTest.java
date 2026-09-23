package com.murqin.nocroptrample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Compiled into both platform builds, so each one checks the rules against its
// own Minecraft classpath.
class TrampleRulesTest {

    @Test
    void emptyFarmlandFollowsEmptySettingOnly() {
        assertTrue(TrampleRules.shouldCancel(true, true, true, false, false));
        assertTrue(TrampleRules.shouldCancel(true, false, true, false, false));
        assertFalse(TrampleRules.shouldCancel(true, true, false, true, true));
        assertFalse(TrampleRules.shouldCancel(true, false, false, true, true));
    }

    @Test
    void playerOnCropFollowsPlayerSetting() {
        assertTrue(TrampleRules.shouldCancel(false, true, false, true, false));
        assertFalse(TrampleRules.shouldCancel(false, true, true, false, true));
    }

    @Test
    void mobOnCropFollowsMobSetting() {
        assertTrue(TrampleRules.shouldCancel(false, false, false, false, true));
        assertFalse(TrampleRules.shouldCancel(false, false, true, true, false));
    }
}
