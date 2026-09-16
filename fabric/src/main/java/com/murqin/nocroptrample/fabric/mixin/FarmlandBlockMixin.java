package com.murqin.nocroptrample.fabric.mixin;

import com.murqin.nocroptrample.TrampleRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for FarmlandBlock to prevent trampling based on configuration.
 * <p>
 * This mixin intercepts the {@code turnToBaseBlock} method which converts farmland
 * to dirt when an entity jumps or falls on it. By injecting at the HEAD with
 * cancellable=true, we can prevent the trampling behavior; the actual decision
 * is shared with the NeoForge platform via {@link TrampleRules}.
 * </p>
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    /**
     * Injects at the HEAD of turnToBaseBlock to intercept trampling attempts.
     * <p>
     * This injection runs before farmland converts to dirt. If the entity is
     * null (natural conversion like dehydration), the method proceeds normally.
     * </p>
     *
     * @param entity the entity causing trampling (null for natural conversion)
     * @param state  the current block state
     * @param level  the world/level
     * @param pos    the block position
     * @param ci     callback info for cancelling the method
     */
    @Inject(method = "turnToBaseBlock", at = @At("HEAD"), cancellable = true)
    private void onTurnToDirt(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (entity == null) {
            // Allow natural conversion (dehydration etc.)
            return;
        }

        if (TrampleRules.shouldPreventTrampling(entity, level, pos)) {
            ci.cancel();
        }
    }
}
