package com.murqin.nocroptrample.fabric.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.murqin.nocroptrample.TrampleRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin for {@link FarmlandBlock} to prevent trampling based on configuration.
 * <p>
 * Wraps the {@code turnToBaseBlock} call inside {@code fallOn} rather than cancelling
 * {@code fallOn} wholesale, so vanilla fall damage from {@code super.fallOn} still applies
 * while the trampling decision (including {@code fallDistance}) stays shared with NeoForge
 * via {@link TrampleRules}.
 * </p>
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @WrapWithCondition(
            method = "fallOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FarmlandBlock;turnToBaseBlock(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private boolean shouldAllowTrample(
            FarmlandBlock instance,
            Entity entity,
            BlockState state,
            Level level,
            BlockPos pos,
            @Local(argsOnly = true) double fallDistance
    ) {
        if (entity == null) {
            // Allow natural conversion (dehydration etc.)
            return true;
        }
        return !TrampleRules.shouldPreventTrampling(entity, level, pos, fallDistance);
    }
}
