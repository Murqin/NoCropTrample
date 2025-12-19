package com.murqin.nocroptrample.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NoCropTrampleCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("nocroptrample")
                // /nocroptrample - show status
                .executes(NoCropTrampleCommand::showStatus)
                
                // /nocroptrample player [on|off]
                .then(CommandManager.literal("player")
                    .executes(context -> showPlayerStatus(context))
                    .then(CommandManager.argument("state", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("on");
                            builder.suggest("off");
                            return builder.buildFuture();
                        })
                        .executes(context -> setPlayerState(context, StringArgumentType.getString(context, "state")))
                    )
                )
                
                // /nocroptrample mob [on|off]
                .then(CommandManager.literal("mob")
                    .executes(context -> showMobStatus(context))
                    .then(CommandManager.argument("state", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("on");
                            builder.suggest("off");
                            return builder.buildFuture();
                        })
                        .executes(context -> setMobState(context, StringArgumentType.getString(context, "state")))
                    )
                )
                
                // /nocroptrample status
                .then(CommandManager.literal("status")
                    .executes(NoCropTrampleCommand::showStatus)
                )
                
                // /nocroptrample reload
                .then(CommandManager.literal("reload")
                    .executes(NoCropTrampleCommand::reloadConfig)
                )
        );
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        source.sendFeedback(() -> Text.literal("§6[NoCropTrample] §fStatus:"), false);
        source.sendFeedback(() -> Text.literal("  §7Player trampling prevention: ")
                .append(getStatusText(ModConfig.preventPlayerTrampling)), false);
        source.sendFeedback(() -> Text.literal("  §7Mob trampling prevention: ")
                .append(getStatusText(ModConfig.preventMobTrampling)), false);
        
        return 1;
    }

    private static int showPlayerStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(() -> Text.literal("§6[NoCropTrample] §7Player trampling prevention: ")
                .append(getStatusText(ModConfig.preventPlayerTrampling)), false);
        return 1;
    }

    private static int showMobStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(() -> Text.literal("§6[NoCropTrample] §7Mob trampling prevention: ")
                .append(getStatusText(ModConfig.preventMobTrampling)), false);
        return 1;
    }

    private static int setPlayerState(CommandContext<ServerCommandSource> context, String state) {
        ServerCommandSource source = context.getSource();
        
        if (!state.equalsIgnoreCase("on") && !state.equalsIgnoreCase("off")) {
            source.sendError(Text.literal("§c[NoCropTrample] Invalid state! Use 'on' or 'off'."));
            return 0;
        }
        
        boolean newState = state.equalsIgnoreCase("on");
        ModConfig.preventPlayerTrampling = newState;
        ModConfig.save();
        
        source.sendFeedback(() -> Text.literal("§6[NoCropTrample] §7Player trampling prevention: ")
                .append(getStatusText(newState)), true);
        
        return 1;
    }

    private static int setMobState(CommandContext<ServerCommandSource> context, String state) {
        ServerCommandSource source = context.getSource();
        
        if (!state.equalsIgnoreCase("on") && !state.equalsIgnoreCase("off")) {
            source.sendError(Text.literal("§c[NoCropTrample] Invalid state! Use 'on' or 'off'."));
            return 0;
        }
        
        boolean newState = state.equalsIgnoreCase("on");
        ModConfig.preventMobTrampling = newState;
        ModConfig.save();
        
        source.sendFeedback(() -> Text.literal("§6[NoCropTrample] §7Mob trampling prevention: ")
                .append(getStatusText(newState)), true);
        
        return 1;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        ModConfig.load();
        
        source.sendFeedback(() -> Text.literal("§6[NoCropTrample] §aConfig reloaded!"), true);
        
        return 1;
    }

    private static Text getStatusText(boolean enabled) {
        return enabled 
            ? Text.literal("ON").formatted(Formatting.GREEN)
            : Text.literal("OFF").formatted(Formatting.RED);
    }
}
