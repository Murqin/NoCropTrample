package com.murqin.nocroptrample.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.StateName;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

/**
 * Handles the /nocroptrample command registration and execution.
 * <p>
 * Provides commands to view and modify trampling prevention settings.
 * Chat messages stay plain English (non-translatable) so vanilla clients
 * without the mod can read them directly.
 * </p>
 */
public class NoCropTrampleCommand {

    private static final int REQUIRED_OP_LEVEL = 2;

    private static final String STATE_ON = "on";
    private static final String STATE_OFF = "off";

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
                                .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                                .executes(NoCropTrampleCommand::reloadConfig))
                        .then(Commands.literal("reset")
                                .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                                .executes(NoCropTrampleCommand::resetConfig)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildToggleCommand(
            String literal,
            StateName stateName,
            com.mojang.brigadier.Command<CommandSourceStack> statusAction) {
        return Commands.literal(literal)
                .executes(statusAction)
                .then(Commands.argument("state", StringArgumentType.word())
                        .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                        .suggests((context, builder) -> {
                            builder.suggest(STATE_ON);
                            builder.suggest(STATE_OFF);
                            return builder.buildFuture();
                        })
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
                        .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                        .suggests((context, builder) -> {
                            for (int i = ConfigSnapshot.MIN_FEATHER_FALLING_LEVEL;
                                 i <= ConfigSnapshot.MAX_FEATHER_FALLING_LEVEL; i++) {
                                builder.suggest(i);
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> setFeatherFallingLevel(
                                context,
                                IntegerArgumentType.getInteger(context, "level"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFallDistanceCommand() {
        return Commands.literal("falldistance")
                .executes(NoCropTrampleCommand::showFallDistanceStatus)
                .then(Commands.argument("blocks", DoubleArgumentType.doubleArg(0.0, 100.0))
                        .requires(source -> checkPermission(source, REQUIRED_OP_LEVEL))
                        .suggests((context, builder) -> {
                            builder.suggest("0");
                            builder.suggest("1.5");
                            builder.suggest("2.0");
                            builder.suggest("3.0");
                            builder.suggest("4.0");
                            builder.suggest("5.0");
                            builder.suggest("10.0");
                            return builder.buildFuture();
                        })
                        .executes(context -> setFallDistance(
                                context,
                                DoubleArgumentType.getDouble(context, "blocks"))));
    }

    private static boolean checkPermission(CommandSourceStack source, int level) {
        PermissionSet permissions = source.permissions();
        if (permissions instanceof LevelBasedPermissionSet levelBased) {
            return levelBased.level().isEqualOrHigherThan(PermissionLevel.byId(level));
        }
        return permissions == PermissionSet.ALL_PERMISSIONS;
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigSnapshot config = ModConfig.snapshot();
        boolean canEdit = checkPermission(source, REQUIRED_OP_LEVEL);

        source.sendSuccess(() -> prefix().append(Component.literal("Status:").withStyle(ChatFormatting.WHITE)), false);

        source.sendSuccess(() -> buildBoolStatusLine(
                LABEL_EMPTY, config.preventEmptyTrampling(), "empty", canEdit), false);
        source.sendSuccess(() -> buildBoolStatusLine(
                LABEL_PLAYER, config.preventPlayerTrampling(), "player", canEdit), false);
        source.sendSuccess(() -> buildBoolStatusLine(
                LABEL_MOB, config.preventMobTrampling(), "mob", canEdit), false);

        source.sendSuccess(() -> buildFeatherFallingStatusLine(config.minFeatherFallingLevel(), canEdit), false);
        source.sendSuccess(() -> buildFallDistanceStatusLine(config.maxSafeFallDistance(), canEdit), false);

        if (canEdit) {
            source.sendSuccess(() -> Component.literal("  ")
                    .append(actionLink("[Reload]", ChatFormatting.AQUA,
                            "/nocroptrample reload", "Reload config from disk"))
                    .append(Component.literal(" "))
                    .append(actionLink("[Reset Defaults]", ChatFormatting.YELLOW,
                            "/nocroptrample reset", "Reset all settings to defaults")), false);
        }

        return 1;
    }

    private static MutableComponent buildBoolStatusLine(
            String label, boolean enabled, String subcommand, boolean canEdit) {
        MutableComponent line = Component.literal("  " + label).withStyle(ChatFormatting.GRAY);
        if (!canEdit) {
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

    private static MutableComponent buildFeatherFallingStatusLine(int level, boolean canEdit) {
        MutableComponent line = Component.literal("  " + LABEL_FEATHER_FALLING).withStyle(ChatFormatting.GRAY)
                .append(formatFeatherFallingComponent(level));
        if (!canEdit) {
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

    private static MutableComponent buildFallDistanceStatusLine(double distance, boolean canEdit) {
        MutableComponent line = Component.literal("  " + LABEL_FALL_DISTANCE).withStyle(ChatFormatting.GRAY)
                .append(formatFallDistanceComponent(distance));
        if (!canEdit) {
            return line;
        }
        if (distance > 0.0) {
            double decreased = Math.max(0.0, Math.round((distance - 0.5) * 2.0) / 2.0);
            line.append(Component.literal(" "))
                    .append(actionLink("[-]", ChatFormatting.YELLOW,
                            "/nocroptrample falldistance " + formatNumber(decreased),
                            "Decrease max safe fall distance by 0.5 blocks"));
        }
        if (distance < 100.0) {
            double increased = Math.min(100.0, Math.round((distance + 0.5) * 2.0) / 2.0);
            line.append(Component.literal(" "))
                    .append(actionLink("[+]", ChatFormatting.AQUA,
                            "/nocroptrample falldistance " + formatNumber(increased),
                            "Increase max safe fall distance by 0.5 blocks"));
        }
        if (distance > 0.0) {
            line.append(Component.literal(" "))
                    .append(actionLink("[Unlimited]", ChatFormatting.GREEN,
                            "/nocroptrample falldistance 0",
                            "Set max safe fall distance to Unlimited (0)"));
        }
        return line;
    }

    private static int showEmptyStatus(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_EMPTY).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(ModConfig.isPreventEmptyTrampling())),
                false);
        return 1;
    }

    private static int showPlayerStatus(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_PLAYER).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(ModConfig.isPreventPlayerTrampling())),
                false);
        return 1;
    }

    private static int showMobStatus(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_MOB).withStyle(ChatFormatting.GRAY))
                        .append(getStatusText(ModConfig.isPreventMobTrampling())),
                false);
        return 1;
    }

    private static int showFeatherFallingStatus(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FEATHER_FALLING).withStyle(ChatFormatting.GRAY))
                        .append(formatFeatherFallingComponent(ModConfig.getMinFeatherFallingLevel())),
                false);
        return 1;
    }

    private static int showFallDistanceStatus(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FALL_DISTANCE).withStyle(ChatFormatting.GRAY))
                        .append(formatFallDistanceComponent(ModConfig.getMaxSafeFallDistance())),
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
                ModConfig.setPreventEmptyTrampling(newState);
                label = LABEL_EMPTY;
            }
            case PLAYER -> {
                ModConfig.setPreventPlayerTrampling(newState);
                label = LABEL_PLAYER;
            }
            case MOB -> {
                ModConfig.setPreventMobTrampling(newState);
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
        ModConfig.setMinFeatherFallingLevel(level);
        int applied = ModConfig.getMinFeatherFallingLevel();
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FEATHER_FALLING).withStyle(ChatFormatting.GRAY))
                        .append(formatFeatherFallingComponent(applied)),
                true);
        return 1;
    }

    private static int setFallDistance(CommandContext<CommandSourceStack> context, double blocks) {
        ModConfig.setMaxSafeFallDistance(blocks);
        double applied = ModConfig.getMaxSafeFallDistance();
        context.getSource().sendSuccess(
                () -> prefix()
                        .append(Component.literal(LABEL_FALL_DISTANCE).withStyle(ChatFormatting.GRAY))
                        .append(formatFallDistanceComponent(applied)),
                true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ModConfig.load();
        context.getSource().sendSuccess(
                () -> prefix().append(Component.literal("Config reloaded!").withStyle(ChatFormatting.GREEN)),
                true);
        return 1;
    }

    private static int resetConfig(CommandContext<CommandSourceStack> context) {
        ModConfig.reset();
        context.getSource().sendSuccess(
                () -> prefix().append(Component.literal("Config reset to defaults!").withStyle(ChatFormatting.GREEN)),
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

    private static @NonNull Component formatFallDistanceComponent(double distance) {
        if (distance <= 0.0) {
            return Component.literal("Unlimited (0)").withStyle(ChatFormatting.GREEN);
        }
        String unit = Math.abs(distance - 1.0) < 1e-6 ? " block" : " blocks";
        return Component.literal(formatNumber(distance) + unit).withStyle(ChatFormatting.AQUA);
    }

    private static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-6) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", "");
    }
}
