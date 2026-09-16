package com.murqin.nocroptrample;

import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Trampling decision shared by the Fabric mixin and the NeoForge event
 * listener, so both platforms enforce the exact same rules.
 */
public final class TrampleRules {

    private TrampleRules() {
    }

    /**
     * @param entity the entity causing the trample (must not be null)
     * @param level  the level containing the farmland
     * @param pos    the farmland's position
     * @return true if the farmland-to-dirt conversion should be cancelled
     */
    public static boolean shouldPreventTrampling(Entity entity, LevelReader level, BlockPos pos) {
        boolean isPlayer = entity instanceof Player;
        boolean isEmpty = !isCropAbove(level, pos);
        return shouldCancel(
                isEmpty,
                isPlayer,
                ModConfig.isPreventEmptyTrampling(),
                ModConfig.isPreventPlayerTrampling(),
                ModConfig.isPreventMobTrampling());
    }

    /**
     * Pure decision, no Minecraft types involved — unit testable without a
     * game runtime.
     */
    static boolean shouldCancel(boolean isEmpty, boolean isPlayer, boolean preventEmpty,
                                 boolean preventPlayer, boolean preventMob) {
        if (isEmpty) {
            return preventEmpty;
        }
        return isPlayer ? preventPlayer : preventMob;
    }

    private static boolean isCropAbove(LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        Block aboveBlock = aboveState.getBlock();
        return aboveBlock instanceof CropBlock
                || aboveBlock instanceof StemBlock
                || aboveBlock instanceof AttachedStemBlock
                // Catches modded crops that opt into the vanilla convention instead
                // of extending one of the classes above.
                || aboveState.is(BlockTags.CROPS);
    }
}
