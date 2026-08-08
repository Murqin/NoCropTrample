package com.murqin.nocroptrample.util;

import com.murqin.nocroptrample.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
    private static final Map<BlockPos, Long> LAST_FEEDBACK_TIMES = new ConcurrentHashMap<>();

    /**
     * Registers cleanup listeners. Must be called once during mod initialization.
     */
    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                LAST_MESSAGE_TIMES.remove(handler.getPlayer().getUUID()));
    }

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

        // Particles and sound are keyed by block position rather than by player, since
        // non-player entities (pets, villagers, mobs) trigger this too and would otherwise
        // spam every tick they stand on protected farmland.
        long currentTime = System.currentTimeMillis();
        long feedbackCooldownMs = ModConfig.getActionBarCooldownSeconds() * 1000L;
        long lastFeedbackTime = LAST_FEEDBACK_TIMES.getOrDefault(pos, 0L);
        boolean offCooldown = currentTime - lastFeedbackTime >= feedbackCooldownMs;

        if (offCooldown) {
            LAST_FEEDBACK_TIMES.put(pos.immutable(), currentTime);

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
        }

        if (ModConfig.isEnableActionBarMessage() && entity instanceof Player player) {
            long lastMessageTime = LAST_MESSAGE_TIMES.getOrDefault(player.getUUID(), 0L);

            if (currentTime - lastMessageTime >= feedbackCooldownMs) {
                player.sendOverlayMessage(Component.translatable("nocroptrample.feedback.protected"));
                LAST_MESSAGE_TIMES.put(player.getUUID(), currentTime);
            }
        }
    }
}
