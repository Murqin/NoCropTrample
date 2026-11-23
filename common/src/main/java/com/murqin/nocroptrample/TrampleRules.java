package com.murqin.nocroptrample;

import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Optional;

/**
 * Trampling decision shared by the Fabric mixin and the NeoForge event
 * listener, so both platforms enforce the exact same rules.
 */
public final class TrampleRules {

    /**
     * Farmland is 15/16 of a block tall (0.0625 below a full block), so stepping off
     * an N-block ledge onto farmland accumulates N + 0.0625 fallDistance (+ float drift).
     */
    static final double FARMLAND_DEPTH_TOLERANCE = (1.0 / 16.0) + 1e-3;

    private TrampleRules() {
    }

    /**
     * @param entity       the entity causing the trample (may be null)
     * @param level        the level containing the farmland
     * @param pos          the farmland's position
     * @param fallDistance the entity's fall distance in blocks
     * @return true if the farmland-to-dirt conversion should be cancelled
     */
    public static boolean shouldPreventTrampling(Entity entity, LevelReader level, BlockPos pos, double fallDistance) {
        if (entity == null || level == null || pos == null) {
            return false;
        }

        Player controllingPlayer = (entity instanceof Player p)
                ? p
                : (entity.getControllingPassenger() instanceof Player p ? p : null);
        boolean isPlayer = controllingPlayer != null;

        if (isEntityOrRiderInTag(entity, controllingPlayer, ModTags.ALWAYS_TRAMPLE_ENTITIES)) {
            return false;
        }
        if (isEntityOrRiderInTag(entity, controllingPlayer, ModTags.TRAMPLE_EXEMPT_ENTITIES)) {
            return true;
        }

        if (level instanceof ServerLevel serverLevel && ModGameRules.isRegistered()) {
            GameRules rules = serverLevel.getGameRules();
            int maxSafeFall = rules.get(ModGameRules.maxSafeFallDistance);
            if (maxSafeFall > 0 && fallDistance > maxSafeFall + FARMLAND_DEPTH_TOLERANCE) {
                return false;
            }

            boolean preventEmpty = rules.get(ModGameRules.preventEmptyTrampling);
            boolean targetRule = isPlayer
                    ? rules.get(ModGameRules.preventPlayerTrampling)
                    : rules.get(ModGameRules.preventMobTrampling);

            // Skip pos.above() allocation and chunk lookup when empty and target agree
            boolean baseAllowed = (preventEmpty == targetRule)
                    ? targetRule
                    : (isCropAbove(level, pos) ? targetRule : preventEmpty);
            if (!baseAllowed) {
                return false;
            }

            if (isPlayer) {
                int minFF = rules.get(ModGameRules.minFeatherFallingLevel);
                if (minFF > 0) {
                    return getFeatherFallingLevel(controllingPlayer, level) >= minFF;
                }
            }
            return true;
        }

        ConfigSnapshot config = ModConfig.snapshot();
        if (config.maxSafeFallDistance() > 0
                && fallDistance > config.maxSafeFallDistance() + FARMLAND_DEPTH_TOLERANCE) {
            return false;
        }

        boolean preventEmpty = config.preventEmptyTrampling();
        boolean targetRule = isPlayer ? config.preventPlayerTrampling() : config.preventMobTrampling();
        boolean baseAllowed = (preventEmpty == targetRule)
                ? targetRule
                : (isCropAbove(level, pos) ? targetRule : preventEmpty);
        if (!baseAllowed) {
            return false;
        }

        if (isPlayer && config.minFeatherFallingLevel() > 0) {
            return getFeatherFallingLevel(controllingPlayer, level) >= config.minFeatherFallingLevel();
        }
        return true;
    }

    /**
     * Pure decision, no Minecraft types involved — unit testable without a
     * game runtime.
     */
    static boolean shouldCancel(
            boolean isEmpty,
            boolean isPlayer,
            boolean alwaysTrample,
            boolean trampleExempt,
            double fallDistance,
            int playerFeatherFallingLevel,
            ConfigSnapshot config) {
        if (alwaysTrample) {
            return false;
        }
        if (trampleExempt) {
            return true;
        }
        if (config.maxSafeFallDistance() > 0
                && fallDistance > config.maxSafeFallDistance() + FARMLAND_DEPTH_TOLERANCE) {
            return false;
        }

        boolean baseAllowed = isEmpty
                ? config.preventEmptyTrampling()
                : (isPlayer ? config.preventPlayerTrampling() : config.preventMobTrampling());
        if (!baseAllowed) {
            return false;
        }

        if (isPlayer && config.minFeatherFallingLevel() > 0) {
            return playerFeatherFallingLevel >= config.minFeatherFallingLevel();
        }
        return true;
    }

    private static boolean isEntityOrRiderInTag(Entity entity, Player controllingPlayer, TagKey<EntityType<?>> tag) {
        if (entity.typeHolder().is(tag)) {
            return true;
        }
        return controllingPlayer != null && controllingPlayer != entity && controllingPlayer.typeHolder().is(tag);
    }

    private static int getFeatherFallingLevel(Player player, LevelReader level) {
        Optional<Registry<Enchantment>> lookup = level.registryAccess().lookup(Registries.ENCHANTMENT);
        if (lookup.isEmpty()) {
            return 0;
        }
        Optional<Holder.Reference<Enchantment>> holder = lookup.get().get(Enchantments.FEATHER_FALLING);
        return holder.isPresent() ? EnchantmentHelper.getEnchantmentLevel(holder.get(), player) : 0;
    }

    private static boolean isCropAbove(LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.isAir()) {
            return false;
        }
        Block aboveBlock = aboveState.getBlock();
        return aboveBlock instanceof CropBlock
                || aboveBlock instanceof StemBlock
                || aboveBlock instanceof AttachedStemBlock
                // Catches modded crops that opt into the vanilla convention instead
                // of extending one of the classes above, plus datapack overrides (e.g. torchflower).
                || aboveState.is(BlockTags.CROPS)
                || aboveState.is(ModTags.PROTECTED_CROPS);
    }
}
