package com.murqin.nocroptrample.mixin;

import com.murqin.nocroptrample.config.ModConfig;
import com.murqin.nocroptrample.util.FeedbackHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for FarmlandBlock to prevent trampling based on configuration.
 * <p>
 * This mixin intercepts the {@code turnToDirt} method which converts farmland
 * to dirt when an entity jumps or falls on it. By injecting at the HEAD with
 * cancellable=true, we can prevent the trampling behavior selectively based
 * on whether the entity is a player, pet, villager, or mob and what the configuration allows.
 * </p>
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    /**
     * Injects at the HEAD of isNearWater to simulate water proximity when preventDehydration is enabled.
     *
     * @param level the level reader
     * @param pos   the farmland block position
     * @param cir   callback info for returning a boolean value
     */
    @Inject(method = "isNearWater", at = @At("HEAD"), cancellable = true)
    private static void onIsNearWater(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isPreventDehydration()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Injects at the HEAD of turnToDirt to intercept trampling attempts and empty farmland reversion.
     * <p>
     * This injection runs before farmland converts to dirt. If the entity is
     * null (natural conversion like dehydration or no crops above), it checks
     * whether empty reversion prevention is enabled.
     * Otherwise, it checks configuration to determine if trampling should be
     * prevented based on entity type and enchantment/equipment checks.
     * </p>
     *
     * @param entity the entity causing trampling (null for natural conversion)
     * @param state  the current block state
     * @param level  the world/level
     * @param pos    the block position
     * @param ci     callback info for cancelling the method
     */
    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void onTurnToDirt(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (entity == null) {
            if (ModConfig.isPreventEmptyReversion()) {
                ci.cancel();
            }
            return;
        }

        Block aboveBlock = level.getBlockState(pos.above()).getBlock();
        boolean isEmpty = !(
            aboveBlock instanceof CropBlock
            || aboveBlock instanceof StemBlock
            || aboveBlock instanceof AttachedStemBlock
        );

        if (isEmpty) {
            if (ModConfig.isPreventEmptyTrampling()) {
                ci.cancel();
                FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
            }
            return;
        }

        // Check if entity is a Pet (TamableAnimal or OwnableEntity)
        if (entity instanceof TamableAnimal || entity instanceof OwnableEntity) {
            if (ModConfig.isPreventPetTrampling()) {
                ci.cancel();
                FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
            }
            return;
        }

        // Check if entity is a Villager
        if (entity instanceof Villager) {
            if (ModConfig.isPreventVillagerTrampling()) {
                ci.cancel();
                FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
            }
            return;
        }

        // Check if entity is a Player
        if (entity instanceof Player player) {
            if (!ModConfig.isPreventPlayerTrampling()) {
                return;
            }

            // Leather Boots check
            if (ModConfig.isProtectWithLeatherBoots() && player.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS)) {
                ci.cancel();
                FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
                return;
            }

            // Feather Falling check
            ModConfig.FeatherFallingMode ffMode = ModConfig.getFeatherFallingMode();
            switch (ffMode) {
                case DISABLED:
                    ci.cancel();
                    FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
                    break;
                case ANY_LEVEL: {
                    int ffLevel = getFeatherFallingLevel(level, player);
                    if (ffLevel > 0) {
                        ci.cancel();
                        FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
                    }
                    break;
                }
                case SCALED: {
                    int ffLevel = getFeatherFallingLevel(level, player);
                    if (ffLevel > 0) {
                        float chance = ffLevel * 0.25f;
                        if (level.getRandom().nextFloat() < chance) {
                            ci.cancel();
                            FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
                        }
                    }
                    break;
                }
            }
            return;
        }

        // Generic Mob check
        if (ModConfig.isPreventMobTrampling()) {
            ci.cancel();
            FeedbackHelper.triggerProtectionFeedback(level, pos, entity);
        }
    }

    private static int getFeatherFallingLevel(Level level, Player player) {
        return level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.FEATHER_FALLING))
                .map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, player))
                .orElse(0);
    }
}
