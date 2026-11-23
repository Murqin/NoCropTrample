package com.murqin.nocroptrample;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Registers and manages NoCropTrample's per-world Minecraft GameRules.
 * <p>
 * Built-in defaults are seeded from {@link ModConfig} at startup so global
 * config sets defaults for new worlds, while active worlds store and persist
 * their own overrides in {@code level.dat}.
 * </p>
 */
public final class ModGameRules {

    public static final Identifier ID_PREVENT_EMPTY =
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "prevent_empty_trampling");
    public static final Identifier ID_PREVENT_PLAYER =
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "prevent_player_trampling");
    public static final Identifier ID_PREVENT_MOB =
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "prevent_mob_trampling");
    public static final Identifier ID_MIN_FEATHER_FALLING =
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "min_feather_falling_level");
    public static final Identifier ID_MAX_SAFE_FALL_DISTANCE =
            Identifier.fromNamespaceAndPath(NoCropTrample.MOD_ID, "max_safe_fall_distance");

    public static volatile GameRule<Boolean> preventEmptyTrampling;
    public static volatile GameRule<Boolean> preventPlayerTrampling;
    public static volatile GameRule<Boolean> preventMobTrampling;
    public static volatile GameRule<Integer> minFeatherFallingLevel;
    public static volatile GameRule<Integer> maxSafeFallDistance;

    private static volatile List<GameRule<?>> allRules = List.of();

    private ModGameRules() {
    }

    public static void registerAll(BiConsumer<Identifier, GameRule<?>> registrar) {
        if (isRegistered()) {
            return;
        }
        ConfigSnapshot defaults = ModConfig.snapshot();

        preventEmptyTrampling = booleanRule(defaults.preventEmptyTrampling());
        preventPlayerTrampling = booleanRule(defaults.preventPlayerTrampling());
        preventMobTrampling = booleanRule(defaults.preventMobTrampling());
        minFeatherFallingLevel = integerRule(
                defaults.minFeatherFallingLevel(),
                ConfigSnapshot.MIN_FEATHER_FALLING_LEVEL,
                ConfigSnapshot.MAX_FEATHER_FALLING_LEVEL);
        maxSafeFallDistance = integerRule(
                defaults.maxSafeFallDistance(),
                ConfigSnapshot.MIN_SAFE_FALL_DISTANCE,
                ConfigSnapshot.MAX_SAFE_FALL_DISTANCE);

        registrar.accept(ID_PREVENT_EMPTY, preventEmptyTrampling);
        registrar.accept(ID_PREVENT_PLAYER, preventPlayerTrampling);
        registrar.accept(ID_PREVENT_MOB, preventMobTrampling);
        registrar.accept(ID_MIN_FEATHER_FALLING, minFeatherFallingLevel);
        registrar.accept(ID_MAX_SAFE_FALL_DISTANCE, maxSafeFallDistance);

        allRules = List.of(
                preventEmptyTrampling,
                preventPlayerTrampling,
                preventMobTrampling,
                minFeatherFallingLevel,
                maxSafeFallDistance);
    }

    public static boolean isRegistered() {
        return !allRules.isEmpty();
    }

    public static List<GameRule<?>> allRules() {
        return allRules;
    }

    public static boolean isModRule(GameRule<?> rule) {
        return rule != null && (rule == preventEmptyTrampling
                || rule == preventPlayerTrampling
                || rule == preventMobTrampling
                || rule == minFeatherFallingLevel
                || rule == maxSafeFallDistance);
    }

    public static ConfigSnapshot snapshot(GameRules rules) {
        if (rules == null || !isRegistered()) {
            return ModConfig.snapshot();
        }
        return new ConfigSnapshot(
                rules.get(preventEmptyTrampling),
                rules.get(preventPlayerTrampling),
                rules.get(preventMobTrampling),
                rules.get(minFeatherFallingLevel),
                rules.get(maxSafeFallDistance));
    }

    public static void setPreventEmptyTrampling(MinecraftServer server, boolean value) {
        setIfChanged(server, preventEmptyTrampling, value);
    }

    public static void setPreventPlayerTrampling(MinecraftServer server, boolean value) {
        setIfChanged(server, preventPlayerTrampling, value);
    }

    public static void setPreventMobTrampling(MinecraftServer server, boolean value) {
        setIfChanged(server, preventMobTrampling, value);
    }

    public static void setMinFeatherFallingLevel(MinecraftServer server, int level) {
        setIfChanged(server, minFeatherFallingLevel, ConfigSnapshot.clampFeatherFallingLevel(level));
    }

    public static void setMaxSafeFallDistance(MinecraftServer server, int blocks) {
        setIfChanged(server, maxSafeFallDistance, ConfigSnapshot.clampFallDistance(blocks));
    }

    public static void applySnapshot(MinecraftServer server, ConfigSnapshot snapshot) {
        setPreventEmptyTrampling(server, snapshot.preventEmptyTrampling());
        setPreventPlayerTrampling(server, snapshot.preventPlayerTrampling());
        setPreventMobTrampling(server, snapshot.preventMobTrampling());
        setMinFeatherFallingLevel(server, snapshot.minFeatherFallingLevel());
        setMaxSafeFallDistance(server, snapshot.maxSafeFallDistance());
    }

    /**
     * Applies a snapshot directly to a {@link GameRules} instance without a server
     * (used to seed newly opened Create World screens from updated defaults).
     */
    public static void applySnapshotToRules(GameRules rules, ConfigSnapshot snapshot) {
        if (rules == null || snapshot == null || !isRegistered()) {
            return;
        }
        rules.set(preventEmptyTrampling, snapshot.preventEmptyTrampling(), null);
        rules.set(preventPlayerTrampling, snapshot.preventPlayerTrampling(), null);
        rules.set(preventMobTrampling, snapshot.preventMobTrampling(), null);
        rules.set(minFeatherFallingLevel, snapshot.minFeatherFallingLevel(), null);
        rules.set(maxSafeFallDistance, snapshot.maxSafeFallDistance(), null);
    }

    private static <T> void setIfChanged(MinecraftServer server, GameRule<T> rule, T value) {
        if (server == null || rule == null) {
            return;
        }
        GameRules rules = server.getGameRules();
        if (!Objects.equals(rules.get(rule), value)) {
            rules.set(rule, value, server);
        }
    }

    private static GameRule<Boolean> booleanRule(boolean defaultValue) {
        return new GameRule<>(
                GameRuleCategory.MISC,
                GameRuleType.BOOL,
                BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean,
                Codec.BOOL,
                b -> b ? 1 : 0,
                defaultValue,
                FeatureFlagSet.of());
    }

    private static GameRule<Integer> integerRule(int defaultValue, int min, int max) {
        return new GameRule<>(
                GameRuleCategory.MISC,
                GameRuleType.INT,
                IntegerArgumentType.integer(min, max),
                GameRuleTypeVisitor::visitInteger,
                Codec.intRange(min, max),
                i -> i,
                defaultValue,
                FeatureFlagSet.of());
    }
}
