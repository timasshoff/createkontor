package com.timder.kontor.game.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.*;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;

import java.util.*;
import java.util.stream.Collectors;

public class CompanyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("company")
                                .then(Commands.literal("found")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(context -> found(context, false))
                                                .then(Commands.argument("withLoan", BoolArgumentType.bool())
                                                        .executes(context -> found(context, BoolArgumentType.getBool(context, "withLoan"))))))
                                .then(Commands.literal("info")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(CompanyCommands::info)))
                                .then(Commands.literal("balance")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                        .executes(CompanyCommands::balance))))));
    }

    private static int found(CommandContext<CommandSourceStack> context, boolean withLoan) {
        CompanySavedData data = CompanySavedData.get(context.getSource().getServer());
        CompanyRegistry registry = data.getRegistry();
        Economy economy = EconomySavedData.get(context.getSource().getServer()).getEconomy();

        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Only a player can open the graph."));
            return 0;
        }

        Company company;
        try {
             company = registry.found(
                    StringArgumentType.getString(context, "name"),
                    economy.currentDay(),
                    player.getUUID(),
                    withLoan,
                    economy.policyRate(),
                    CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()))
            );
            data.setDirty();
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Something went wrong: " + e.getMessage()));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Founded company with name: " + company.name() + " (" + company.id() + ")."), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        String name = StringArgumentType.getString(context, "name");

        var registry = CompanySavedData.get(server).getRegistry();
        Optional<Company> found = registry.findByName(name);
        if (found.isEmpty()) {
            source.sendFailure(Component.literal("No company named \"" + name + "\"."));
            return 0;
        }
        Company company = found.get();

        CompanyParams params = CompanyConfig.toCompanyParams(new LegalForms(KontorData.getLegalFormDefinitions()));

        String legalForm;
        try {
            legalForm = "Level " + company.legalLevel() + " " + company.legalForm(params).id();
        } catch (IllegalArgumentException e) {
            legalForm = "Level " + company.legalLevel() + " (legal form data unavailable)";
        }

        String managers = company.managers().isEmpty()
                ? "none"
                : company.managers().stream().map(id -> playerName(server, id)).collect(Collectors.joining(", "));

        String liquidity = switch (company.liquidity()) {
            case NORMAL -> "normal";
            case ILLIQUIDITY -> "in trouble for " + company.daysInTrouble() + " day" + (company.daysInTrouble() == 1 ? "" : "s");
        };

        List<CompanyHistoryEntry> history = company.history();
        String lastResult = history.isEmpty()
                ? "No settled days yet."
                : formatHistoryEntry(company, history.get(history.size() - 1));

        String text = String.format(Locale.ROOT,
                "=== %s (ID %d, %s) ===\n" +
                        "Founded: Day %d\n" +
                        "Owner: %s\n" +
                        "Managers: %s\n" +
                        "\n" +
                        "Balance: %s (%s)\n" +
                        "Net worth: %s\n" +
                        "Outstanding loans: %s (%d)\n" +
                        "\n" +
                        "Open requests: %d | Open orders: %d\n" +
                        "%s",
                company.name(), company.id().value(), legalForm,
                company.foundingDay(),
                playerName(server, company.owner()),
                managers,
                company.account().getBalance(), liquidity,
                company.netWorth(),
                company.totalDebt(), company.loans().size(),
                company.requestBoard().openRequestsTotal(), company.orderBook().openOrders(),
                lastResult);

        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int balance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        String name = StringArgumentType.getString(context, "name");

        CompanySavedData data = CompanySavedData.get(server);
        var registry = data.getRegistry();
        Optional<Company> found = registry.findByName(name);
        if (found.isEmpty()) {
            source.sendFailure(Component.literal("No company named \"" + name + "\"."));
            return 0;
        }
        Company company = found.get();
        Economy economy = EconomySavedData.get(context.getSource().getServer()).getEconomy();

        company.account().book(economy.currentDay(), BookingKind.DEPOSIT, Money.fromDollar(DoubleArgumentType.getDouble(context, "amount")), "fraud");
        data.setDirty();

        source.sendSuccess(() -> Component.literal("Successfully booked."), false);
        return 1;
    }

    private static String formatHistoryEntry(Company company, CompanyHistoryEntry entry) {
        Map<BookingKind, Money> sums = company.account().sumsByKind(entry.day());

        StringBuilder text = new StringBuilder();
        text.append("Last day result (Day ").append(entry.day()).append("): ").append(entry.result())
                .append("\n  Revenue: ").append(entry.revenue());

        sums.entrySet().stream()
                .filter(e -> e.getKey().isCost())
                .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
                .forEach(e -> text.append("\n  ").append(formatBookingKind(e.getKey())).append(": ").append(e.getValue()));

        List<Map.Entry<BookingKind, Money>> financing = sums.entrySet().stream()
                .filter(e -> e.getKey().isFinancing())
                .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
                .toList();
        if (!financing.isEmpty()) {
            text.append("\nFinancing today (not part of the result above):");
            for (Map.Entry<BookingKind, Money> e : financing) {
                text.append("\n  ").append(formatBookingKind(e.getKey())).append(": ").append(e.getValue());
            }
        }

        return text.toString();
    }

    private static String formatBookingKind(BookingKind kind) {
        String name = kind.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String playerName(MinecraftServer server, UUID id) {
        ServerPlayer online = server.getPlayerList().getPlayer(id);
        if (online != null) {
            return online.getGameProfile().getName();
        }
        GameProfileCache cache = server.getProfileCache();
        if (cache != null) {
            Optional<GameProfile> profile = cache.get(id);
            if (profile.isPresent()) {
                return profile.get().getName();
            }
        }
        return id.toString();
    }
}
