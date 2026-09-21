package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.chart.ChartSpec;
import com.timder.kontor.chart.MarketCharts;
import com.timder.kontor.chart.RawMaterialCharts;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketRules;
import com.timder.kontor.core.market.MarketSnapshot;
import com.timder.kontor.core.raw.RawMaterialHistoryEntry;
import com.timder.kontor.core.raw.RawMaterialSnapshot;
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
import java.util.Locale;

public class RawMaterialCommands {

    private static final int DEFAULT_GRAPH_DAYS = 30;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("raw")
                                .then(Commands.literal("info")
                                        .then(Commands.argument("material", ResourceLocationArgument.id())
                                                .executes(RawMaterialCommands::info)))
                                .then(Commands.literal("graph")
                                        .then(Commands.argument("material", ResourceLocationArgument.id())
                                                .executes(context -> graph(context, DEFAULT_GRAPH_DAYS))
                                                .then(Commands.argument("days", IntegerArgumentType.integer(1, Economy.HISTORY_LENGTH_DAYS))
                                                        .executes(context -> graph(context, IntegerArgumentType.getInteger(context, "days"))))))));

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

    private static int graph(CommandContext<CommandSourceStack> context, int days) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only a player can open the graph."));
            return 0;
        }

        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "material");
        ItemId itemId = new ItemId(location.toString());

        List<RawMaterialHistoryEntry> history;

        try {
            history = economy.rawMaterialHistory(itemId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no raw material for " + location));
            return 0;
        }

        if (history.isEmpty()) {
            source.sendFailure(Component.literal("There is no history for " + location + " yet."));
            return 0;
        }

        int wanted = days * MarketRules.TRADING_TICKS_PER_DAY;
        List<RawMaterialHistoryEntry> window = history.subList(Math.max(0, history.size() - wanted), history.size());

        ChartSpec spec;
        try {
            spec = RawMaterialCharts.full(location, window);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Cannot build the chart: " + e.getMessage()));
            return 0;
        }

        PacketDistributor.sendToPlayer(player, new ChartPayload(spec));
        source.sendSuccess(() -> Component.literal("Opening graph for " + location + " (" + window.size() + " points)."), false);

        return 1;
    }
}
