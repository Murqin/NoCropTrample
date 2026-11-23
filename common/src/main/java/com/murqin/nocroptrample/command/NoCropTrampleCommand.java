package com.murqin.nocroptrample.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.murqin.nocroptrample.ModGameRules;
import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.StateName;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permissions;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Handles the /nocroptrample command registration and execution.
 * <p>
 * Reads and modifies the active world's GameRules; {@code reload} reloads
 * {@code config/nocroptrample.json} and applies it to the active world.
 * Chat messages stay plain English (non-translatable) so vanilla clients
 * without the mod can read them directly.
 * </p>
 */
public class NoCropTrampleCommand {

    private static final String STATE_ON = "on";
    private static final String STATE_OFF = "off";

    private static final List<String> STATE_SUGGESTIONS = List.of(STATE_ON, STATE_OFF);
    private static final List<String> FEATHER_FALLING_SUGGESTIONS = List.of("0", "1", "2", "3", "4");
    private static final List<String> FALL_DISTANCE_SUGGESTIONS = List.of("0", "1", "2", "3", "4", "5", "10");

    private static final String LABEL_EMPTY = "Empty farmland trampling prevention: ";
    private static final String LABEL_PLAYER = "Player trampling prevention: ";
    private static final String LABEL_MOB = "Mob trampling prevention: ";
    private static final String LABEL_FEATHER_FALLING = "Min Feather Falling level: ";
    private static final String LABEL_FALL_DISTANCE = "Max safe fall distance: ";

    /**
     * Registers the /nocroptrample command with all its subcommands.
     *
     * @param dispatcher the command dispatcher to register commands with
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("nocroptrample")
                        .executes(NoCropTrampleCommand::showStatus)
                        .then(buildToggleCommand("empty", StateName.EMPTY, NoCropTrampleCommand::showEmptyStatus))
                        .then(buildToggleCommand("player", StateName.PLAYER, NoCropTrampleCommand::showPlayerStatus))
                        .then(buildToggleCommand("mob", StateName.MOB, NoCropTrampleCommand::showMobStatus))
                        .then(buildFeatherFallingCommand())
                        .then(buildFallDistanceCommand())
                        .then(Commands.literal("status")
                                .executes(NoCropTrampleCommand::showStatus))
                        .then(Commands.literal("reload")
                                .requires(NoCropTrampleCommand::hasEditPermission)
                                .executes(NoCropTrampleCommand::reloadConfig))
                        .then(Commands.literal("reset")
                                .requires(NoCropTrampleCommand::hasEditPermission)
                                .executes(NoCropTrampleCommand::resetConfig)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildToggleCommand(
            String literal,
            StateName stateName,
            com.mojang.brigadier.Command<CommandSourceStack> statusAction) {
        return Commands.literal(literal)
                .executes(statusAction)
                .then(Commands.argument("state", StringArgumentType.word())
                        .requires(NoCropTrampleCommand::hasEditPermission)
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(STATE_SUGGESTIONS, builder))
                        .executes(context -> setState(
                                context,
                                StringArgumentType.getString(context, "state"),
                                stateName)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFeatherFallingCommand() {
        return Commands.literal("featherfalling")
                .executes(NoCropTrampleCommand::showFeatherFallingStatus)
                .then(Commands.argument("level", IntegerArgumentType.integer(
                                ConfigSnapshot.MIN_FEATHER_FALLING_LEVEL,
                                ConfigSnapshot.MAX_FEATHER_FALLING_LEVEL))
                        .requires(NoCropTrampleCommand::hasEditPermission)
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(FEATHER_FALLING_SUGGESTIONS, builder))
                        .executes(context -> setFeatherFallingLevel(
                                context,
                                IntegerArgumentType.getInteger(context, "level"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFallDistanceCommand() {
        return Commands.literal("falldistance")
                .executes(NoCropTrampleCommand::showFallDistanceStatus)
                .then(Commands.argument("blocks", IntegerArgumentType.integer(
                                ConfigSnapshot.MIN_SAFE_FALL_DISTANCE,
                                ConfigSnapshot.MAX_SAFE_FALL_DISTANCE))
                        .requires(NoCropTrampleCommand::hasEditPermission)
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(FALL_DISTANCE_SUGGESTIONS, builder))
                        .executes(context -> setFallDistance(
                                context,
                                IntegerArgumentType.getInteger(context, "blocks"))));
    }

    private static boolean hasEditPermission(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static ConfigSnapshot currentSnapshot(CommandSourceStack source) {
        return ModGameRules.snapshot(source.getServer().getGameRules());
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigSnapshot config = currentSnapshot(source);
        boolean showControls = hasEditPermission(source) && source.isPlayer();

        source.sendSuccess(() -> prefix().append(Component.literal("Status:").withStyle(ChatFormatting.WHITE)), false);

        source.sendSuccess(() -> buildBoolStatusLine(
                LABEL_EMPTY, config.preventEmptyTrampling(), "empty", showControls), false);
        source.sendSuccess(() -> buildBoolStatusLine(
                LABEL_PLAYER, config.preventPlayerTrampling(), "player", showControls), false);
        source.sendSuccess(() -> buildBoolStatusLine(
                LABEL_MOB, config.preventMobTrampling(), "mob", showControls), false);

        source.sendSuccess(() -> buildFeatherFallingStatusLine(config.minFeatherFallingLevel(), showControls), false);
        source.sendSuccess(() -> buildFallDistanceStatusLine(config.maxSafeFallDistance(), showControls), false);

        if (showControls) {
            source.sendSuccess(() -> Component.literal("  ")
                    .append(actionLink("[Reload]", ChatFormatting.AQUA,
                            "/nocroptrample reload", "Reload defaults from config file and apply to this world"))
                    .append(Component.literal(" "))
                    .append(actionLink("[Reset Defaults]", ChatFormatting.YELLOW,
                            "/nocroptrample reset", "Reset this world's rules to defaults")), false);
        }

        return 1;
    }

    private static MutableComponent buildBoolStatusLine(
            String label, boolean enabled, String subcommand, boolean showControls) {
        MutableComponent line = Component.literal("  " + label).withStyle(ChatFormatting.GRAY);
        if (!showControls) {
            return line.append(getStatusText(enabled));
        }
        String nextState = enabled ? STATE_OFF : STATE_ON;
        String hover = "Click to turn " + (enabled ? "OFF" : "ON");
        return line.append(actionLink(
                enabled ? "[ON]" : "[OFF]",
                enabled ? ChatFormatting.GREEN : ChatFormatting.RED,
                "/nocroptrample " + subcommand + " " + nextState,
                hover));
    }

    private static MutableComponent buildFeatherFallingStatusLine(int level, boolean showControls) {
        MutableComponent line = Component.literal("  " + LABEL_FEATHER_FALLING).withStyle(ChatFormatting.GRAY)
                .append(formatFeatherFallingComponent(level));
        if (!showControls) {
            return line;
        }
        if (level > ConfigSnapshot.MIN_FEATHER_FALLING_LEVEL) {
            line.append(Component.literal(" "))
                    .append(actionLink("[-]", ChatFormatting.YELLOW,
                            "/nocroptrample featherfalling " + (level - 1),
                            "Decrease minimum Feather Falling level"));
        }
        if (level < ConfigSnapshot.MAX_FEATHER_FALLING_LEVEL) {
            line.append(Component.literal(" "))
                    .append(actionLink("[+]", ChatFormatting.AQUA,
                            "/nocroptrample featherfalling " + (level + 1),
                            "Increase minimum Feather Falling level"));
        }
        return line;
    }

    private static MutableComponent buildFallDistanceStatusLine(int distance, boolean showControls) {
        MutableComponent line = Component.literal("  " + LABEL_FALL_DISTANCE).withStyle(ChatFormatting.GRAY)
                .append(formatFallDistanceComponent(distance));
        if (!showControls) {
            return line;
        }
        if (distance > ConfigSnapshot.MIN_SAFE_FALL_DISTANCE) {
            line.append(Component.literal(" "))
                    .append(actionLink("[-]", ChatFormatting.YELLOW,
                            "/nocroptrample falldistance " + (distance - 1),
                            "Decrease max safe fall distance by 1 block"));
        }
        if (distance < ConfigSnapshot.MAX_SAFE_FALL_DISTANCE) {
            line.append(Component.literal(" "))
                    .append(actionLink("[+]", ChatFormatting.AQUA,
                            "/nocroptrample falldistance " + (distance + 1),
                            "Increase max safe fall distance by 1 block"));
        }
        if (distance > ConfigSnapshot.MIN_SAFE_FALL_DISTANCE) {
            line.append(Component.literal(" "))
                    .append(actionLink("[Unlimited]", ChatFormatting.GREEN,
                            "/nocroptrample falldistance 0",
                            "Set max safe fall distance to Unlimited (0)"));
        }
        return line;
    }

    private static int showEmptyStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_EMPTY).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(currentSnapshot(source).preventEmptyTrampling())),
                false);
        return 1;
    }

    private static int showPlayerStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_PLAYER).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(currentSnapshot(source).preventPlayerTrampling())),
                false);
        return 1;
    }

    private static int showMobStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_MOB).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(currentSnapshot(source).preventMobTrampling())),
                false);
        return 1;
    }

    private static int showFeatherFallingStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FEATHER_FALLING).withStyle(ChatFormatting.GRAY))
                        .append(formatFeatherFallingComponent(currentSnapshot(source).minFeatherFallingLevel())),
                false);
        return 1;
    }

    private static int showFallDistanceStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FALL_DISTANCE).withStyle(ChatFormatting.GRAY))
                        .append(formatFallDistanceComponent(currentSnapshot(source).maxSafeFallDistance())),
                false);
        return 1;
    }

    private static int setState(CommandContext<CommandSourceStack> context, String state, StateName stateName) {
        CommandSourceStack source = context.getSource();

        if (!state.equalsIgnoreCase(STATE_ON) && !state.equalsIgnoreCase(STATE_OFF)) {
            source.sendFailure(prefixError()
                    .append(Component.literal("Invalid state! Use 'on' or 'off'.").withStyle(ChatFormatting.RED)));
            return 0;
        }

        boolean newState = state.equalsIgnoreCase(STATE_ON);
        String label;

        switch (stateName) {
            case EMPTY -> {
                ModGameRules.setPreventEmptyTrampling(source.getServer(), newState);
                label = LABEL_EMPTY;
            }
            case PLAYER -> {
                ModGameRules.setPreventPlayerTrampling(source.getServer(), newState);
                label = LABEL_PLAYER;
            }
            case MOB -> {
                ModGameRules.setPreventMobTrampling(source.getServer(), newState);
                label = LABEL_MOB;
            }
            default -> {
                NoCropTrample.LOGGER.error("Tried setting state of {}, which is not a valid state!", stateName);
                label = "Couldn't set unknown prevention type to: ";
            }
        }

        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(label).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(newState)),
                true);

        return 1;
    }

    private static int setFeatherFallingLevel(CommandContext<CommandSourceStack> context, int level) {
        CommandSourceStack source = context.getSource();
        ModGameRules.setMinFeatherFallingLevel(source.getServer(), level);
        int applied = currentSnapshot(source).minFeatherFallingLevel();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FEATHER_FALLING).withStyle(ChatFormatting.GRAY))
                        .append(formatFeatherFallingComponent(applied)),
                true);
        return 1;
    }

    private static int setFallDistance(CommandContext<CommandSourceStack> context, int blocks) {
        CommandSourceStack source = context.getSource();
        ModGameRules.setMaxSafeFallDistance(source.getServer(), blocks);
        int applied = currentSnapshot(source).maxSafeFallDistance();
        source.sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FALL_DISTANCE).withStyle(ChatFormatting.GRAY))
                        .append(formatFallDistanceComponent(applied)),
                true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!ModConfig.reload()) {
            source.sendFailure(prefixError().append(Component.literal(
                    "Failed to reload config/nocroptrample.json — kept active world rules unchanged. Check server logs.")
                    .withStyle(ChatFormatting.RED)));
            return 0;
        }
        ModGameRules.applySnapshot(source.getServer(), ModConfig.snapshot());
        source.sendSuccess(
                () -> prefix().append(
                        Component.literal("Config reloaded and applied to this world!").withStyle(ChatFormatting.GREEN)),
                true);
        return 1;
    }

    private static int resetConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ModGameRules.applySnapshot(source.getServer(), ConfigSnapshot.DEFAULTS);
        source.sendSuccess(
                () -> prefix().append(Component.literal("World rules reset to defaults!").withStyle(ChatFormatting.GREEN)),
                true);
        return 1;
    }

    private static MutableComponent prefix() {
        return Component.literal("[NoCropTrample] ").withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent prefixError() {
        return Component.literal("[NoCropTrample] ").withStyle(ChatFormatting.RED);
    }

    private static MutableComponent actionLink(
            String label, ChatFormatting color, String command, String hoverText) {
        return Component.literal(label).withStyle(style -> style
                .withColor(color)
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverText))));
    }

    private static @NonNull Component getStatusText(boolean enabled) {
        return enabled
                ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                : Component.literal("OFF").withStyle(ChatFormatting.RED);
    }

    private static @NonNull Component formatFeatherFallingComponent(int level) {
        return level <= 0
                ? Component.literal("OFF (0)").withStyle(ChatFormatting.YELLOW)
                : Component.literal("Level " + level).withStyle(ChatFormatting.AQUA);
    }

    private static @NonNull Component formatFallDistanceComponent(int distance) {
        if (distance <= 0) {
            return Component.literal("Unlimited (0)").withStyle(ChatFormatting.GREEN);
        }
        String unit = distance == 1 ? " block" : " blocks";
        return Component.literal(distance + unit).withStyle(ChatFormatting.AQUA);
    }
}
