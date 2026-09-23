package com.timder.kontor.core.company.order;

import com.timder.kontor.core.company.LegalFormDef;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class OrderBook {
    private final List<Order> orders = new ArrayList<>();

    /**
     * @param legalForm The company's current legal form
     * @return True, if another order could be added right now
     */
    public boolean hasRoom(LegalFormDef legalForm) {
        Objects.requireNonNull(legalForm, "legalForm must not be null.");
        return openOrders() < legalForm.maxOpenOrders();
    }

    /**
     * How many orders count against the legal forms order book limit.
     * @return The number of occupying open orders
     */
    public int openOrders() {
        int count = 0;
        for (Order order : orders) {
            if (order.getOrigin().occupiesOrderBookSlot()) {
                count++;
            }
        }
        return count;
    }

    public List<Order> allOrders() {
        return List.copyOf(orders);
    }

    /**
     * @param orderNumber The orders number
     * @return The order
     */
    public Order get(long orderNumber) {
        return find(orderNumber);
    }

    /**
     * Adds an order to the book
     * @param order The order to add
     * @param legalForm The company's current legal form
     * @throws IllegalStateException If the order would occupy a slot and there is no more room left
     */
    public void add(Order order, LegalFormDef legalForm) {
        Objects.requireNonNull(order, "order must not be null.");
        Objects.requireNonNull(legalForm, "legalForm must not be null.");
        if (order.getOrigin().occupiesOrderBookSlot() && !hasRoom(legalForm)) {
            throw new IllegalStateException("The order book has no room.");
        }
        orders.add(order);
    }

    /**
     * Removes an order from the book after it has been settled (K 12.7)
     * @param orderNumber The order's number
     * @return The removed order
     * @throws NoSuchElementException If no open order with this number exists
     */
    public Order remove(long orderNumber) {
        Order order = find(orderNumber);
        orders.remove(order);
        return order;
    }

    /**
     * Records a (partial) delivery against an order
     * @param orderNumber The order's number
     * @param amount The delivered amount
     */
    public void deliver(long orderNumber, int amount) {
        find(orderNumber).recordDelivery(amount);
    }

    public List<Order> advance(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must not be negative");

        List<Order> enteredGracePeriod = new ArrayList<>();
        for (Order order : orders) {
            if (order.getPhase() == OrderPhase.GRACE_PERIOD) {
                order.reduceRemainingGracePeriodTicks(ticks);
                continue;
            }

            long remaining = order.remainingDeadlineTicks();
            if (ticks < remaining) {
                order.reduceRemainingDeadlineTicks(ticks);
            } else {
                long overflow = ticks - remaining;
                order.reduceRemainingDeadlineTicks(remaining);
                order.enterGracePeriod();
                if (overflow > 0) {
                    order.reduceRemainingGracePeriodTicks(overflow);
                }
                enteredGracePeriod.add(order);
            }
        }
        return enteredGracePeriod;
    }

    private Order find(long orderNumber) {
        for (Order order : orders) {
            if (order.getNumber() == orderNumber) {
                return order;
            }
        }
        throw new NoSuchElementException("No open order with number " + orderNumber + ".");
    }

    public record SaveState(List<Order.SaveState> orders) {
        public SaveState {
            orders = List.copyOf(orders);
        }
    }

    public SaveState getSaveState() {
        List<Order.SaveState> orderStates = new ArrayList<>();
        for (Order order : orders) {
            orderStates.add(order.getSaveState());
        }
        return new SaveState(orderStates);
    }

    public static OrderBook restore(SaveState saveState) {
        OrderBook book = new OrderBook();
        for (Order.SaveState orderState : saveState.orders()) {
            book.orders.add(Order.restore(orderState));
        }
        return book;
    }
}
