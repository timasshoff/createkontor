package com.timder.kontor.game.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderPhase;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public class OrderCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("kontor")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("order")
                                .then(Commands.literal("list")
                                        .then(Commands.argument("company", StringArgumentType.string())
                                                .executes(OrderCommands::list)))));
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Company company = CommandSupport.findCompany(context, "company");
        if (company == null) {
            return 0;
        }

        List<Order> orders = company.orderBook().allOrders();
        StringBuilder text = new StringBuilder("=== ").append(company.name()).append(": open orders (")
                .append(orders.size()).append(") ===");
        if (orders.isEmpty()) {
            text.append("\n  none");
        }
        for (Order order : orders) {
            boolean grace = order.getPhase() == OrderPhase.GRACE_PERIOD;
            text.append(String.format(Locale.ROOT, "\n  #%d %s: %d/%d delivered x %.2f, %s left %s",
                    order.getNumber(), order.getProduct(), order.getDeliveredQuantity(), order.getQuantity(), order.getUnitPrice(),
                    grace ? "GRACE PERIOD," : "deadline",
                    CommandSupport.formatTicks(grace ? order.remainingGracePeriodTicks() : order.remainingDeadlineTicks())));
        }

        source.sendSuccess(() -> Component.literal(text.toString()), false);
        return 1;
    }
}
