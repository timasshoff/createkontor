package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.core.company.order.RequestOrigin;
import com.timder.kontor.core.company.request.*;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketDefinition;
import com.timder.kontor.core.market.MarketParticipant;
import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.port.SeededRng;
import com.timder.kontor.core.value.ItemId;
import com.timder.kontor.data.KontorData;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public class RequestCommands {

    /** Requests are added to the company's own list price. Sales representatives do not exist yet, so the factor is 1. */
    private static final double SALES_REP_FACTOR = 1.0;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("request")
                                .then(Commands.literal("spawn")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .then(Commands.argument("product", ResourceLocationArgument.id())
                                                        .executes(context -> spawn(context, null, null))
                                                        .then(Commands.argument("quantityFactor", IntegerArgumentType.integer(1))
                                                                .executes(context -> spawn(context,
                                                                        IntegerArgumentType.getInteger(context, "quantityFactor"), null))
                                                                .then(Commands.argument("urgency", DoubleArgumentType.doubleArg(0.0, 1.0))
                                                                        .executes(context -> spawn(context,
                                                                                IntegerArgumentType.getInteger(context, "quantityFactor"),
                                                                                DoubleArgumentType.getDouble(context, "urgency"))))))))
                                .then(Commands.literal("accept")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .then(Commands.argument("number", LongArgumentType.longArg(0))
                                                        .executes(RequestCommands::accept))))
                                .then(Commands.literal("list")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .executes(RequestCommands::list)))));
    }

    private static int spawn(CommandContext<CommandSourceStack> context, Integer quantityFactor, Double urgency) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }

        Economy economy = EconomySavedData.get(server).getEconomy();
        ResourceLocation location = ResourceLocationArgument.getId(context, "product");
        ItemId product = new ItemId(location.toString());

        if (!economy.marketIds().contains(product)) {
            source.sendFailure(Component.literal("There is no market for " + location + "."));
            return 0;
        }
        if (!economy.isParticipant(product, company.id())) {
            source.sendFailure(Component.literal(company.name() + " does not take part in the market for " + location
                    + ", so it gets no requests for it. Use /kontor market register first."));
            return 0;
        }

        CompanyParams companyParams = CommandSupport.companyParams(source);
        LegalFormDef legalForm = companyParams == null ? null : CommandSupport.legalForm(source, company, companyParams);
        if (legalForm == null) {
            return 0;
        }
        RequestParams requestParams = CommandSupport.requestParams(source);
        if (requestParams == null) {
            return 0;
        }

        int packageSize = economy.packageSize(product);
        if (packageSize > legalForm.maxOrderQuantity()) {
            source.sendFailure(Component.literal(legalForm.id() + " allows orders of at most " + legalForm.maxOrderQuantity()
                    + ", but a package of " + location + " has " + packageSize + "."));
            return 0;
        }

        Rng rng = SeededRng.forName(server.overworld().getSeed(), "request_spawn:" + server.overworld().getGameTime());
        int factor;
        if (quantityFactor == null) {
            factor = RequestRules.drawQuantityFactor(requestParams, rng);
        } else {
            if (requestParams.quantitySteps().stream().noneMatch(step -> step.k() == quantityFactor)) {
                source.sendFailure(Component.literal("A request can only have the quantity factor "
                        + requestParams.quantitySteps().stream().map(step -> String.valueOf(step.k())).toList() + "."));
                return 0;
            }
            factor = quantityFactor;
        }
        double delta = urgency == null ? RequestRules.drawUrgency(requestParams, rng) : urgency;

        RequestArrivals.Arrival arrival = RequestArrivals.arrive(company, economy, product, legalForm, requestParams, factor, delta);
        CompanySavedData.get(server).setDirty();

        if (arrival.lost()) {
            RequestBoard board = company.requestBoard();
            source.sendSuccess(() -> Component.literal("The board of " + company.name() + " is full (per product "
                    + legalForm.maxOpenRequestsPerProduct() + ", total " + legalForm.maxOpenRequestsTotal()
                    + "). The request for " + location + " is lost, " + board.lostRequestsToday(product)
                    + " lost today."), false);
            return 0;
        }

        Request request = arrival.request();
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Request #%d for %s: %d x %.2f (quantity factor %d, urgency %.2f)\n" +
                        "Deadline %s at manufacturing depth %d, offer open for %s.\n" +
                        "Accept it with /kontor request accept \"%s\" %d",
                request.getNumber(), location, request.getQuantity(), request.getUnitPrice(), factor, delta,
                CommandSupport.formatTicks(request.getDeadlineTicks()), economy.manufacturingDepth(product),
                CommandSupport.formatTicks(request.remainingOfferTicks()), company.name(), request.getNumber())), false);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }
        long number = LongArgumentType.getLong(context, "number");

        CompanyParams companyParams = CommandSupport.companyParams(source);
        RequestParams requestParams = CommandSupport.requestParams(source);
        if (companyParams == null || requestParams == null) {
            return 0;
        }

        Order order;
        try {
            order = company.acceptRequest(number, companyParams, requestParams);
        } catch (NoSuchElementException e) {
            source.sendFailure(Component.literal(company.name() + " has no open request with number " + number + "."));
            return 0;
        } catch (IllegalStateException e) {
            source.sendFailure(Component.literal("Cannot accept request " + number + ": " + e.getMessage()));
            return 0;
        }
        CompanySavedData.get(source.getServer()).setDirty();

        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Accepted request #%d as order #%d: %d x %s at %.2f. Deadline %s, then a grace period of %s.",
                number, order.getNumber(), order.getQuantity(), order.getProduct(), order.getUnitPrice(),
                CommandSupport.formatTicks(order.getDeadlineTicks()), CommandSupport.formatTicks(order.getGracePeriodTicks()))), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }

        List<Request> requests = company.requestBoard().allOpenRequests();
        StringBuilder text = new StringBuilder("=== ").append(company.name()).append(": open requests (")
                .append(requests.size()).append(") ===");
        if (requests.isEmpty()) {
            text.append("\n  none");
        }
        for (Request request : requests) {
            text.append(String.format(Locale.ROOT, "\n  #%d %s: %d x %.2f, deadline %s, offer left %s",
                    request.getNumber(), request.getProduct(), request.getQuantity(), request.getUnitPrice(),
                    CommandSupport.formatTicks(request.getDeadlineTicks()), CommandSupport.formatTicks(request.remainingOfferTicks())));
        }

        Map<ItemId, Integer> lost = company.requestBoard().lostRequestsToday();
        if (!lost.isEmpty()) {
            text.append("\nLost today because the board was full:");
            lost.forEach((product, count) -> text.append("\n  ").append(product).append(": ").append(count));
        }

        MinecraftServer server = source.getServer();
        Economy economy = EconomySavedData.get(server).getEconomy();
        RequestArrivals arrivals = CompanySavedData.get(server).getArrivals(server);
        StringBuilder countdowns = new StringBuilder();
        for (ItemId market : economy.marketIds()) {
            if (economy.isParticipant(market, company.id())) {
                OptionalLong ticks = arrivals.ticksUntilNextRequest(company.id(), market);
                countdowns.append("\n  ").append(market).append(": ")
                        .append(ticks.isPresent() ? "next request in " + CommandSupport.formatTicks(ticks.getAsLong()) : "no request expected");
            }
        }
        if (!countdowns.isEmpty()) {
            text.append("\nNext arrival:").append(countdowns);
        }

        source.sendSuccess(() -> Component.literal(text.toString()), false);
        return 1;
    }

    private static double listPriceOf(Economy economy, ItemId product, Company company) {
        for (MarketParticipant participant : economy.participants(product)) {
            if (participant.companyId().equals(company.id())) {
                return participant.listPrice();
            }
        }
        throw new IllegalStateException(company.id() + " is not registered on " + product + ".");
    }

    private static long nextRequestNumber(Company company) {
        long highest = 0;
        for (Request request : company.requestBoard().allOpenRequests()) {
            highest = Math.max(highest, request.getNumber());
        }
        for (Order order : company.orderBook().allOrders()) {
            if (order.getOrigin() instanceof RequestOrigin origin) {
                highest = Math.max(highest, origin.requestNumber());
            }
        }
        return highest + 1;
    }
}
