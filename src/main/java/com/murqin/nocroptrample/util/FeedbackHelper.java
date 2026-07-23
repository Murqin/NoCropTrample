package com.murqin.nocroptrample.util;

import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper utility for triggering visual, audio, and text feedback when farmland trampling is prevented.
 */
public class FeedbackHelper {

    private static final Map<UUID, Long> LAST_MESSAGE_TIMES = new ConcurrentHashMap<>();

    /**
     * Triggers protection feedback (particles, sound, and action bar message) based on configuration.
     *
     * @param level  the world/level
     * @param pos    the block position of the farmland
     * @param entity the entity that attempted to trample the farmland
     */
    public static void triggerProtectionFeedback(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }

        if (ModConfig.isEnableParticles() && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    5,
                    0.2,
                    0.1,
                    0.2,
                    0.05
            );
        }

        if (ModConfig.isEnableSound()) {
            level.playSound(
                    (Player) null,
                    pos,
                    SoundEvents.CROP_BREAK,
                    SoundSource.BLOCKS,
                    1.0f,
                    1.5f
            );
        }

        if (ModConfig.isEnableActionBarMessage() && entity instanceof Player player) {
            long currentTime = System.currentTimeMillis();
            long cooldownMs = ModConfig.getActionBarCooldownSeconds() * 1000L;
            long lastTime = LAST_MESSAGE_TIMES.getOrDefault(player.getUUID(), 0L);

            if (currentTime - lastTime >= cooldownMs) {
                player.sendOverlayMessage(Component.literal("Farmland protected!"));
                LAST_MESSAGE_TIMES.put(player.getUUID(), currentTime);
            }
        }
    }
}
