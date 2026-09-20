package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;

public class EconomyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("economy")
                                .then(Commands.literal("advance")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                                .executes(EconomyCommands::advance)))));
    }

    private static int advance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        EconomySavedData data = EconomySavedData.get(source.getServer());
        Economy economy = data.getEconomy();

        int days = IntegerArgumentType.getInteger(context, "days");
        economy.advanceTicksQuietly(days * Economy.DAY_LENGTH);
        data.setDirty();

        source.sendSuccess(() -> Component.literal("Advanced the economy by " + days + " days."), false);

        return 1;
    }
}
