package com.murqin.nocroptrample.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles the /nocroptrample command registration and execution.
 * <p>
 * Provides commands to view and modify trampling prevention settings.
 * </p>
 */
public class NoCropTrampleCommand {

        // Constants for permissions
        private static final int REQUIRED_OP_LEVEL = 2;

        // State constants
        private static final String STATE_ON = "on";
        private static final String STATE_OFF = "off";

        // Message formatting constants - the mod tag itself isn't translated, only the labels/messages that follow it
        private static final String PREFIX = "§6[NoCropTrample] ";
        private static final String PREFIX_ERROR = "§c[NoCropTrample] ";
        private static final Component LABEL_STATUS = Component.translatable("nocroptrample.label.status").withStyle(ChatFormatting.WHITE);
        private static final Component LABEL_PRESET = label("nocroptrample.label.preset");
        private static final Component LABEL_EMPTY = label("nocroptrample.label.empty");
        private static final Component LABEL_PLAYER = label("nocroptrample.label.player");
        private static final Component LABEL_MOB = label("nocroptrample.label.mob");
        private static final Component LABEL_FEATHER_FALLING = label("nocroptrample.label.feather_falling");
        private static final Component LABEL_LEATHER_BOOTS = label("nocroptrample.label.leather_boots");
        private static final Component LABEL_PET = label("nocroptrample.label.pet");
        private static final Component LABEL_VILLAGER = label("nocroptrample.label.villager");
        private static final Component LABEL_DEHYDRATION = label("nocroptrample.label.dehydration");
        private static final Component LABEL_EMPTY_REVERSION = label("nocroptrample.label.empty_reversion");
        private static final Component LABEL_PARTICLES = label("nocroptrample.label.particles");
        private static final Component LABEL_SOUND = label("nocroptrample.label.sound");
        private static final Component LABEL_ACTION_BAR = label("nocroptrample.label.action_bar");
        private static final Component LABEL_COOLDOWN = label("nocroptrample.label.cooldown");

        private static MutableComponent label(String key) {
                return Component.translatable(key).withStyle(ChatFormatting.GRAY);
        }

        /**
         * Registers the /nocroptrample command with all its subcommands.
         *
         * @param dispatcher the command dispatcher to register commands with
         */
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("nocroptrample")
                                                // /nocroptrample - show status
                                                .executes(NoCropTrampleCommand::showStatus)

                                                // /nocroptrample preset [vanilla_plus|casual|hardcore|custom]
                                                .then(buildPresetCommand())

                                                // /nocroptrample featherfalling [always|require_feather_falling|scaled_by_level]
                                                .then(buildFeatherFallingCommand())

                                                // /nocroptrample cooldown [0-60]
                                                .then(buildCooldownCommand())

                                                // Boolean toggles
                                                .then(buildBooleanCommand("empty", ModConfig::isPreventEmptyTrampling,
                                                                ModConfig::setPreventEmptyTrampling, LABEL_EMPTY))
                                                .then(buildBooleanCommand("player", ModConfig::isPreventPlayerTrampling,
                                                                ModConfig::setPreventPlayerTrampling, LABEL_PLAYER))
                                                .then(buildBooleanCommand("mob", ModConfig::isPreventMobTrampling,
                                                                ModConfig::setPreventMobTrampling, LABEL_MOB))
                                                .then(buildBooleanCommand("leatherboots", ModConfig::isProtectWithLeatherBoots,
                                                                ModConfig::setProtectWithLeatherBoots, LABEL_LEATHER_BOOTS))
                                                .then(buildBooleanCommand("pet", ModConfig::isPreventPetTrampling,
                                                                ModConfig::setPreventPetTrampling, LABEL_PET))
                                                .then(buildBooleanCommand("villager", ModConfig::isPreventVillagerTrampling,
                                                                ModConfig::setPreventVillagerTrampling, LABEL_VILLAGER))
                                                .then(buildBooleanCommand("dehydration", ModConfig::isPreventDehydration,
                                                                ModConfig::setPreventDehydration, LABEL_DEHYDRATION))
                                                .then(buildBooleanCommand("emptyreversion", ModConfig::isPreventEmptyReversion,
                                                                ModConfig::setPreventEmptyReversion, LABEL_EMPTY_REVERSION))
                                                .then(buildBooleanCommand("particles", ModConfig::isEnableParticles,
                                                                ModConfig::setEnableParticles, LABEL_PARTICLES))
                                                .then(buildBooleanCommand("sound", ModConfig::isEnableSound,
                                                                ModConfig::setEnableSound, LABEL_SOUND))
                                                .then(buildBooleanCommand("actionbar", ModConfig::isEnableActionBarMessage,
                                                                ModConfig::setEnableActionBarMessage, LABEL_ACTION_BAR))

                                                // /nocroptrample status
                                                .then(Commands.literal("status")
                                                                .executes(NoCropTrampleCommand::showStatus))

                                                // /nocroptrample reload
                                                .then(Commands.literal("reload")
                                                                .requires(source -> checkPermission(source,
                                                                                REQUIRED_OP_LEVEL))
                                                                .executes(NoCropTrampleCommand::reloadConfig)));
        }

        /**
         * Builds a subcommand for a boolean setting, handling both the status-only form
         * ({@code /nocroptrample <name>}) and the {@code on}/{@code off} setter form.
         * Extracted so each of the eleven boolean toggles doesn't need its own near-identical
         * command builder and handler pair.
         *
         * @param name   the subcommand literal
         * @param getter reads the current value from {@link ModConfig}
         * @param setter writes a new value to {@link ModConfig}
         * @param label  the chat label used for both the status and confirmation messages
         * @return the built command node
         */
        private static LiteralArgumentBuilder<CommandSourceStack> buildBooleanCommand(
                        String name, Supplier<Boolean> getter, Consumer<Boolean> setter, Component label) {
                return Commands.literal(name)
                                .executes(context -> showBooleanStatus(context, getter, label))
                                .then(Commands.argument("state", StringArgumentType.word())
                                                .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                                                .suggests((context, builder) -> {
                                                        builder.suggest(STATE_ON);
                                                        builder.suggest(STATE_OFF);
                                                        return builder.buildFuture();
                                                })
                                                .executes(context -> setBooleanState(context, setter, label)));
        }

        private static int showBooleanStatus(CommandContext<CommandSourceStack> context, Supplier<Boolean> getter, Component label) {
                CommandSourceStack source = context.getSource();
                source.sendSuccess(
                                () -> Component.literal(PREFIX).append(label).append(getStatusText(getter.get())),
                                false);
                return 1;
        }

        private static int setBooleanState(CommandContext<CommandSourceStack> context, Consumer<Boolean> setter, Component label) {
                CommandSourceStack source = context.getSource();
                String state = StringArgumentType.getString(context, "state");

                if (!state.equalsIgnoreCase(STATE_ON) && !state.equalsIgnoreCase(STATE_OFF)) {
                        source.sendFailure(Component.literal(PREFIX_ERROR).append(Component.translatable("nocroptrample.message.invalid_state")));
                        return 0;
                }

                boolean newState = state.equalsIgnoreCase(STATE_ON);
                setter.accept(newState);

                source.sendSuccess(
                                () -> Component.literal(PREFIX).append(label).append(getStatusText(newState)),
                                true);
                return 1;
        }

        private static LiteralArgumentBuilder<CommandSourceStack> buildPresetCommand() {
            return Commands.literal("preset")
                            .executes(NoCropTrampleCommand::showPresetStatus)
                            .then(Commands.argument("preset", StringArgumentType.word())
                                    .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                                    .suggests((context, builder) -> {
                                        builder.suggest("vanilla_plus");
                                        builder.suggest("casual");
                                        builder.suggest("hardcore");
                                        builder.suggest("custom");
                                        return builder.buildFuture();
                                    })
                                    .executes(NoCropTrampleCommand::setPresetCommand));
        }

        private static LiteralArgumentBuilder<CommandSourceStack> buildFeatherFallingCommand() {
            return Commands.literal("featherfalling")
                            .executes(NoCropTrampleCommand::showFeatherFallingStatus)
                            .then(Commands.argument("mode", StringArgumentType.word())
                                    .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                                    .suggests((context, builder) -> {
                                        for (ModConfig.FeatherFallingMode mode : ModConfig.FeatherFallingMode.values()) {
                                            builder.suggest(mode.name().toLowerCase(Locale.ROOT));
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(NoCropTrampleCommand::setFeatherFallingCommand));
        }

        private static LiteralArgumentBuilder<CommandSourceStack> buildCooldownCommand() {
            return Commands.literal("cooldown")
                            .executes(NoCropTrampleCommand::showCooldownStatus)
                            .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 60))
                                    .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                                    .executes(NoCropTrampleCommand::setCooldownCommand));
        }

        /**
         * Checks if the command source has the required permission level.
         *
         * @param source the command source
         * @param level  the required permission level
         * @return true if the source has permission, false otherwise
         */
        private static boolean checkPermission(CommandSourceStack source, int level) {
                net.minecraft.server.permissions.PermissionSet permissions = source.permissions();
                if (permissions instanceof net.minecraft.server.permissions.LevelBasedPermissionSet levelBased) {
                        return levelBased.level().isEqualOrHigherThan(
                                net.minecraft.server.permissions.PermissionLevel.byId(level));
                }
                return permissions == net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS;
        }

        /**
         * Shows the full status of all trampling prevention settings.
         *
         * @param context the command context
         * @return command result code (1 for success)
         */
        private static int showStatus(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();

            source.sendSuccess(() -> Component.literal(PREFIX).append(LABEL_STATUS), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_PRESET)
                .append(presetName(ModConfig.getActivePreset()).withStyle(ChatFormatting.GOLD)), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_EMPTY)
                .append(getStatusText(ModConfig.isPreventEmptyTrampling())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_PLAYER)
                .append(getStatusText(ModConfig.isPreventPlayerTrampling())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_MOB)
                .append(getStatusText(ModConfig.isPreventMobTrampling())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_FEATHER_FALLING)
                .append(featherFallingModeName(ModConfig.getFeatherFallingMode()).withStyle(ChatFormatting.GOLD)), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_LEATHER_BOOTS)
                .append(getStatusText(ModConfig.isProtectWithLeatherBoots())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_PET)
                .append(getStatusText(ModConfig.isPreventPetTrampling())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_VILLAGER)
                .append(getStatusText(ModConfig.isPreventVillagerTrampling())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_DEHYDRATION)
                .append(getStatusText(ModConfig.isPreventDehydration())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_EMPTY_REVERSION)
                .append(getStatusText(ModConfig.isPreventEmptyReversion())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_PARTICLES)
                .append(getStatusText(ModConfig.isEnableParticles())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_SOUND)
                .append(getStatusText(ModConfig.isEnableSound())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_ACTION_BAR)
                .append(getStatusText(ModConfig.isEnableActionBarMessage())), false);
            source.sendSuccess(() -> Component.literal("  ").append(LABEL_COOLDOWN)
                .append(Component.literal(String.valueOf(ModConfig.getActionBarCooldownSeconds())).withStyle(ChatFormatting.GOLD)), false);

            return 1;
        }

        private static int showPresetStatus(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            source.sendSuccess(
                    () -> Component.literal(PREFIX).append(LABEL_PRESET)
                            .append(presetName(ModConfig.getActivePreset()).withStyle(ChatFormatting.GOLD)),
                    false);
            return 1;
        }

        private static int setPresetCommand(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            String presetName = StringArgumentType.getString(context, "preset");

            ModConfig.ModPreset selectedPreset = null;
            for (ModConfig.ModPreset preset : ModConfig.ModPreset.values()) {
                if (preset.name().equalsIgnoreCase(presetName)) {
                    selectedPreset = preset;
                    break;
                }
            }

            if (selectedPreset == null) {
                source.sendFailure(Component.literal(PREFIX_ERROR).append(Component.translatable("nocroptrample.message.invalid_preset")));
                return 0;
            }

            ModConfig.setPreset(selectedPreset);

            final ModConfig.ModPreset finalPreset = selectedPreset;
            source.sendSuccess(
                    () -> Component.literal(PREFIX).append(Component.translatable("nocroptrample.message.preset_set"))
                            .append(presetName(finalPreset).withStyle(ChatFormatting.GOLD)),
                    true);

            return 1;
        }

        private static int showFeatherFallingStatus(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            source.sendSuccess(
                    () -> Component.literal(PREFIX).append(LABEL_FEATHER_FALLING)
                            .append(featherFallingModeName(ModConfig.getFeatherFallingMode()).withStyle(ChatFormatting.GOLD)),
                    false);
            return 1;
        }

        private static int setFeatherFallingCommand(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            String modeName = StringArgumentType.getString(context, "mode");

            ModConfig.FeatherFallingMode selectedMode = null;
            for (ModConfig.FeatherFallingMode mode : ModConfig.FeatherFallingMode.values()) {
                if (mode.name().equalsIgnoreCase(modeName)) {
                    selectedMode = mode;
                    break;
                }
            }

            if (selectedMode == null) {
                source.sendFailure(Component.literal(PREFIX_ERROR).append(Component.translatable("nocroptrample.message.invalid_feather_falling_mode")));
                return 0;
            }

            ModConfig.setFeatherFallingMode(selectedMode);

            final ModConfig.FeatherFallingMode finalMode = selectedMode;
            source.sendSuccess(
                    () -> Component.literal(PREFIX).append(LABEL_FEATHER_FALLING)
                            .append(featherFallingModeName(finalMode).withStyle(ChatFormatting.GOLD)),
                    true);

            return 1;
        }

        private static int showCooldownStatus(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            source.sendSuccess(
                    () -> Component.literal(PREFIX).append(LABEL_COOLDOWN)
                            .append(Component.literal(String.valueOf(ModConfig.getActionBarCooldownSeconds())).withStyle(ChatFormatting.GOLD)),
                    false);
            return 1;
        }

        private static int setCooldownCommand(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            int seconds = IntegerArgumentType.getInteger(context, "seconds");

            ModConfig.setActionBarCooldownSeconds(seconds);

            source.sendSuccess(
                    () -> Component.literal(PREFIX).append(LABEL_COOLDOWN)
                            .append(Component.literal(String.valueOf(ModConfig.getActionBarCooldownSeconds())).withStyle(ChatFormatting.GOLD)),
                    true);

            return 1;
        }

        /**
         * Reloads the configuration from disk.
         *
         * @param context the command context
         * @return command result code (1 for success)
         */
        private static int reloadConfig(CommandContext<CommandSourceStack> context) {
                CommandSourceStack source = context.getSource();

                ModConfig.load();

                source.sendSuccess(() -> Component.literal(PREFIX)
                                .append(Component.translatable("nocroptrample.message.config_reloaded").withStyle(ChatFormatting.GREEN)),
                                true);

                return 1;
        }

        private static @NonNull MutableComponent presetName(ModConfig.ModPreset preset) {
                String key = switch (preset) {
                        case VANILLA_PLUS -> "nocroptrample.preset.vanilla_plus";
                        case CASUAL -> "nocroptrample.preset.casual";
                        case HARDCORE -> "nocroptrample.preset.hardcore";
                        case CUSTOM -> "nocroptrample.preset.custom";
                };
                return Component.translatable(key);
        }

        private static @NonNull MutableComponent featherFallingModeName(ModConfig.FeatherFallingMode mode) {
                String key = switch (mode) {
                        case ALWAYS -> "nocroptrample.feather_falling.always";
                        case REQUIRE_FEATHER_FALLING -> "nocroptrample.feather_falling.require_feather_falling";
                        case SCALED_BY_LEVEL -> "nocroptrample.feather_falling.scaled_by_level";
                };
                return Component.translatable(key);
        }

        /**
         * Creates a colored component showing ON (green) or OFF (red).
         *
         * @param enabled the state to display
         * @return formatted component
         */
        private static @NonNull Component getStatusText(boolean enabled) {
                return enabled
                                ? Component.translatable("nocroptrample.status.on").withStyle(ChatFormatting.GREEN)
                                : Component.translatable("nocroptrample.status.off").withStyle(ChatFormatting.RED);
        }
}
