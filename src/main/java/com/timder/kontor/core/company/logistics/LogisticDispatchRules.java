package com.timder.kontor.core.company.logistics;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.order.DeliveryRules;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;

import java.util.*;

public final class LogisticDispatchRules {

    public record Line(ItemId product, int quantity, long orderNumber) {
        public Line {
            Objects.requireNonNull(product, "product must not be null.");
            if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1.");
        }
    }

    /**
     * Plans what to order based off order book. Stock is not changed.
     * @param company The company
     * @param economy The economy
     * @param stock The stock of each product in the network
     * @return The lines in the order they were planned
     */
    public static List<Line> plan(Company company, Economy economy, Map<ItemId, Integer> stock) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(stock, "stock must not be null.");

        if (!company.logistics().isConnected() || !company.isActive()) {
            return List.of();
        }

        Set<ItemId> products = new LinkedHashSet<>();
        for (Order order : company.orderBook().allOrders()) {
            products.add(order.getProduct());
        }

        List<Line> lines = new ArrayList<>();
        for (ItemId product : products) {
            int available = Math.max(0, stock.getOrDefault(product, 0));
            for (Order order : DeliveryRules.getOrdersByPriority(company, economy, product)) {
                if (available < 1) {
                    break;
                }

                int remaining = order.remainingQuantity();
                if (remaining < 1) {
                    continue;
                }
                int quantity = Math.min(remaining, available);
                lines.add(new Line(product, quantity, order.getNumber()));
                available -= quantity;
            }
        }
        return List.copyOf(lines);
    }

    /**
     * @param lines The lines of a plan
     * @return The planned amount per product, in the order the products first appear in the lines
     */
    public static Map<ItemId, Integer> totalsByProduct(List<Line> lines) {
        Objects.requireNonNull(lines, "lines must not be null.");
        Map<ItemId, Integer> totals = new LinkedHashMap<>();
        for (Line line : lines) {
            totals.merge(line.product(), line.quantity(), Integer::sum);
        }
        return totals;
    }
}
