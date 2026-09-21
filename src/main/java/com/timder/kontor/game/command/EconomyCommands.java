package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.chart.ChartSpec;
import com.timder.kontor.chart.EconomyCharts;
import com.timder.kontor.chart.RawMaterialCharts;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.macro.MacroHistoryEntry;
import com.timder.kontor.core.raw.RawMaterialHistoryEntry;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.game.EconomySavedData;
import com.timder.kontor.game.network.ChartPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class EconomyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("economy")
                                .then(Commands.literal("advance")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1))
                                                .executes(EconomyCommands::advance)))
                                .then(Commands.literal("cycle")
                                    .executes(EconomyCommands::cycle))
                                .then(Commands.literal("recordDelivery")
                                        .then(Commands.argument("product", ResourceLocationArgument.id())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(EconomyCommands::recordDelivery))))
                                .then(Commands.literal("graph")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1, 360))
                                                .executes(EconomyCommands::graph)))));
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

    private static int cycle(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        EconomySavedData data = EconomySavedData.get(source.getServer());
        Economy economy = data.getEconomy();

        source.sendSuccess(() -> Component.literal("Current cycle: " + economy.currentPhase() + "."), false);
        return 1;
    }

    private static int recordDelivery(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();

        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId itemId = new ItemId(location.toString());

        int amount = IntegerArgumentType.getInteger(context, "amount");

        try {
            economy.recordDelivery(itemId, amount);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no market for " + location));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Recorded delivery for: " + itemId), false);
        return 1;
    }

    private static int graph(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only a player can open the graph."));
            return 0;
        }

        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();

        int days = IntegerArgumentType.getInteger(context, "days");

        List<MacroHistoryEntry> history = economy.macroHistory();
        List<MacroHistoryEntry> window = history.subList(Math.max(0, history.size() - days), history.size());

        ChartSpec spec;
        try {
            spec = EconomyCharts.full(window);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Cannot build the chart: " + e.getMessage()));
            return 0;
        }

        PacketDistributor.sendToPlayer(player, new ChartPayload(spec));
        source.sendSuccess(() -> Component.literal("Opening economy graph (" + window.size() + " points)."), false);
        return 1;
    }
}
