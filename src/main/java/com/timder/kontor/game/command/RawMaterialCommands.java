package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.raw.RawMaterialSnapshot;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class RawMaterialCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("raw")
                                .then(Commands.literal("info")
                                        .then(Commands.argument("material", ResourceLocationArgument.id())
                                                .executes(RawMaterialCommands::info)))));
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "material");
        ItemId itemId = new ItemId(location.toString());

        RawMaterialSnapshot snapshot;
        try {
            snapshot = economy.rawMaterialSnapshot(itemId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no raw material called " + location));
            return 0;
        }

        String text = String.format(Locale.ROOT,
                "=== %s ===\nPrice: %.2f",
                location,
                snapshot.price());

        source.sendSuccess(() -> Component.literal(text), false);

        return 1;
    }
}
