package com.murqin.nocroptrample.config;

/**
 * Immutable snapshot of all trampling rules configuration values.
 * Clamps and sanitizes out-of-range values on construction.
 */
public record ConfigSnapshot(
        boolean preventEmptyTrampling,
        boolean preventPlayerTrampling,
        boolean preventMobTrampling,
        int minFeatherFallingLevel,
        int maxSafeFallDistance
) {
    public static final int MIN_FEATHER_FALLING_LEVEL = 0;
    public static final int MAX_FEATHER_FALLING_LEVEL = 4;
    public static final int MIN_SAFE_FALL_DISTANCE = 0;
    public static final int MAX_SAFE_FALL_DISTANCE = 100;

    public static final ConfigSnapshot DEFAULTS = new ConfigSnapshot(true, true, true, 0, 0);

    public ConfigSnapshot {
        minFeatherFallingLevel = clampFeatherFallingLevel(minFeatherFallingLevel);
        maxSafeFallDistance = clampFallDistance(maxSafeFallDistance);
    }

    public static int clampFeatherFallingLevel(int level) {
        return Math.clamp(level, MIN_FEATHER_FALLING_LEVEL, MAX_FEATHER_FALLING_LEVEL);
    }

    public static int clampFallDistance(int distance) {
        return Math.clamp(distance, MIN_SAFE_FALL_DISTANCE, MAX_SAFE_FALL_DISTANCE);
    }

    /**
     * Converts legacy floating-point JSON values into clamped integer blocks.
     */
    public static int sanitizeFallDistance(double distance) {
        if (!Double.isFinite(distance) || distance <= 0.0) {
            return 0;
        }
        return Math.clamp(Math.round(distance), MIN_SAFE_FALL_DISTANCE, MAX_SAFE_FALL_DISTANCE);
    }
}
