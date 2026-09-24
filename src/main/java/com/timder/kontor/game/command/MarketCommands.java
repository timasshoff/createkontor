package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.chart.ChartSpec;
import com.timder.kontor.chart.MarketCharts;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketRules;
import com.timder.kontor.core.market.MarketSnapshot;
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

public class MarketCommands {

    private static final int DEFAULT_GRAPH_DAYS = 30;
    private static final double START_REPUTATION = 50.0;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("market")
                                .then(Commands.literal("info")
                                        .then(Commands.argument("product", ResourceLocationArgument.id())
                                                .executes(MarketCommands::info)))
                                .then(Commands.literal("graph")
                                        .then(Commands.argument("product", ResourceLocationArgument.id())
                                                .executes(context -> graph(context, DEFAULT_GRAPH_DAYS))
                                                .then(Commands.argument("days", IntegerArgumentType.integer(1, Economy.HISTORY_LENGTH_DAYS))
                                                        .executes(context -> graph(context, IntegerArgumentType.getInteger(context, "days"))))))
                                .then(Commands.literal("register")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .then(Commands.argument("product", ResourceLocationArgument.id())
                                                        .executes(MarketCommands::register))))));
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Economy economy = EconomySavedData.get(source.getServer()).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId itemId = new ItemId(location.toString());

        MarketSnapshot snapshot;
        try {
            snapshot = economy.marketSnapshot(itemId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no market for " + location));
            return 0;
        }

        String text = String.format(Locale.ROOT,
                "=== %s ===\n" +
                        "Price: %.2f (Reference-Price: %.2f, Deviation: %+.1f%%)\n" +
                        "Competitors: %d\n" +
                        "Demand: %.1f | Utilisation: %.1f%% | Overflow: %.1f",
                location,
                snapshot.displayedPrice(),
                snapshot.referenceCost(),
                snapshot.deviation() * 100.0,
                snapshot.visibleCompanies(),
                snapshot.demand(),
                snapshot.utilisation() * 100.0,
                snapshot.overflow());

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
        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId itemId = new ItemId(location.toString());

        MarketSnapshot snapshot;
        List<MarketHistoryEntry> history;

        try {
            snapshot = economy.marketSnapshot(itemId);
            history = economy.marketHistory(itemId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("There is no market for " + location));
            return 0;
        }

        if (history.isEmpty()) {
            source.sendFailure(Component.literal("There is no history for " + location + " yet."));
            return 0;
        }

        int wanted = days * MarketRules.TRADING_TICKS_PER_DAY;
        List<MarketHistoryEntry> window = history.subList(Math.max(0, history.size() - wanted), history.size());

        ChartSpec spec;
        try {
            spec = MarketCharts.full(location, snapshot.referenceCost(), window);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Cannot build the chart: " + e.getMessage()));
            return 0;
        }

        PacketDistributor.sendToPlayer(player, new ChartPayload(spec));
        source.sendSuccess(() -> Component.literal("Opening graph for " + location + " (" + window.size() + " points)."), false);

        return 1;
    }

    private static int register(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }

        EconomySavedData economyData = EconomySavedData.get(source.getServer());
        Economy economy = economyData.getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId product = new ItemId(location.toString());

        if (!economy.marketIds().contains(product)) {
            source.sendFailure(Component.literal("There is no market for " + location + "."));
            return 0;
        }
        if (economy.isParticipant(product, company.id())) {
            source.sendFailure(Component.literal(company.name() + " already takes part in the market for " + location + "."));
            return 0;
        }
        if (!company.isOperational()) {
            source.sendFailure(Component.literal(company.name() + " is in payment difficulties and cannot take on a new product."));
            return 0;
        }

        CompanyParams companyParams = CommandSupport.companyParams(source);
        LegalFormDef legalForm = companyParams == null ? null : CommandSupport.legalForm(source, company, companyParams);
        if (legalForm == null) {
            return 0;
        }
        long products = economy.marketIds().stream().filter(market -> economy.isParticipant(market, company.id())).count();
        if (products >= legalForm.maxProductLicenses()) {
            source.sendFailure(Component.literal(company.name() + " already offers " + products + " product(s), the limit of " + legalForm.id() + " is " + legalForm.maxProductLicenses() + "."));
            return 0;
        }

        double listPrice = economy.marketSnapshot(product).displayedPrice();
        economy.registerParticipant(product, company.id(), listPrice, START_REPUTATION);
        economyData.setDirty();

        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "%s now takes part in the market for %s (list price %.2f, reputation %.0f, product %d of %s).\nNo licence fee is booked.",
                company.name(), location, listPrice, START_REPUTATION, products + 1,
                legalForm.maxProductLicenses() == LegalFormDef.UNLIMITED ? "unlimited" : String.valueOf(legalForm.maxProductLicenses()))), false);
        return 1;
    }
}
