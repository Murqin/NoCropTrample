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
        double maxSafeFallDistance
) {
    public static final int MIN_FEATHER_FALLING_LEVEL = 0;
    public static final int MAX_FEATHER_FALLING_LEVEL = 4;

    public static final ConfigSnapshot DEFAULTS = new ConfigSnapshot(true, true, true, 0, 0.0);

    public ConfigSnapshot {
        minFeatherFallingLevel = clampFeatherFallingLevel(minFeatherFallingLevel);
        maxSafeFallDistance = sanitizeFallDistance(maxSafeFallDistance);
    }

    public static int clampFeatherFallingLevel(int level) {
        return Math.clamp(level, MIN_FEATHER_FALLING_LEVEL, MAX_FEATHER_FALLING_LEVEL);
    }

    public static double sanitizeFallDistance(double distance) {
        return (Double.isFinite(distance) && distance > 0.0) ? distance : 0.0;
    }
}
