package com.timder.kontor.core.company.order;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DeliveryRules {

    private static final Comparator<Order> PRIORITY = Comparator
            .comparingInt((Order order) -> order.getPhase() == OrderPhase.GRACE_PERIOD ? 0 : 1)
            .thenComparingLong(DeliveryRules::remainingTicks)
            .thenComparingLong(Order::getNumber);

    /**
     * All orders of a company for a specific product that can be completed right now.
     * Most urgent orders first.
     * @param company The company
     * @param economy The economy
     * @param product The product
     * @return The orders in priority order
     */
    public static List<Order> getOrdersByPriority(Company company, Economy economy, ItemId product) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(product, "product must not be null.");

        if (!takesPart(company, economy, product)) {
            return List.of();
        }

        List<Order> candidates = new ArrayList<>();
        for (Order order : company.orderBook().allOrders()) {
            if (order.getProduct().equals(product) && canStillBeFulfilled(order)) {
                candidates.add(order);
            }
        }

        candidates.sort(PRIORITY);
        return List.copyOf(candidates);
    }

    public static OrderSettlementResult deliverAndComplete(Company company, Economy economy, long day, Order order) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(order, "order must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (!company.orderBook().allOrders().contains(order)) throw new IllegalArgumentException("The order is not in the company's order book.");
        ItemId product = order.getProduct();
        if (!takesPart(company, economy, product)) throw new IllegalStateException(company.id() + " does not take part in the market of " + product + ".");
        if (!canStillBeFulfilled(order)) throw new IllegalStateException("Order " + order.getNumber() + " can no longer be fulfilled.");

        boolean late = order.getPhase() == OrderPhase.GRACE_PERIOD;
        int quantity = order.getQuantity();

        int remaining = order.remainingQuantity();
        if (remaining > 0) {
            company.orderBook().deliver(order.getNumber(), remaining);
        }
        OrderSettlementResult result = late
                ? OrderRules.settleLate(company, day, order)
                : OrderRules.settleOnTime(company, day, order);
        company.orderBook().remove(order.getNumber());

        economy.recordDelivery(product, company.id(), quantity);
        economy.recordFulfillment(product, company.id());
        return result;
    }

    private static long remainingTicks(Order order) {
        return order.getPhase() == OrderPhase.GRACE_PERIOD
                ? order.remainingGracePeriodTicks()
                : order.remainingDeadlineTicks();
    }

    private static boolean takesPart(Company company, Economy economy, ItemId product) {
        return economy.marketIds().contains(product) && economy.isParticipant(product, company.id());
    }

    private static boolean canStillBeFulfilled(Order order) {
        return order.getPhase() != OrderPhase.GRACE_PERIOD || order.remainingGracePeriodTicks() > 0;
    }

}
