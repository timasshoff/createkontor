package com.timder.kontor.core.company.order;

import com.timder.kontor.core.company.request.Request;
import com.timder.kontor.core.value.ItemId;

import java.util.Objects;

public final class Order {
    private final long number;
    private final ItemId product;
    private final int quantity;
    private final double unitPrice;
    private final double urgency;
    private final long deadlineTicks;
    private final long gracePeriodTicks;
    private final OrderOrigin origin;

    private int deliveredQuantity;
    private long remainingDeadlineTicks;
    private long remainingGracePeriodTicks;
    private OrderPhase phase;

    public Order(long number, ItemId product, int quantity, double unitPrice, double urgency, long deadlineTicks, long gracePeriodTicks, OrderOrigin origin) {
        Objects.requireNonNull(product, "product must not be null.");
        Objects.requireNonNull(origin, "origin must not be null.");
        if (number < 0) throw new IllegalArgumentException("number must not be negative.");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive.");
        if (unitPrice <= 0) throw new IllegalArgumentException("unitPrice must be positive.");
        if (urgency < 0.0 || urgency > 1.0) throw new IllegalArgumentException("urgency must be between 0 and 1.");
        if (deadlineTicks <= 0) throw new IllegalArgumentException("deadlineTicks must be positive.");
        if (gracePeriodTicks < 0) throw new IllegalArgumentException("gracePeriodTicks must not be negative.");

        this.number = number;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.urgency = urgency;
        this.deadlineTicks = deadlineTicks;
        this.gracePeriodTicks = gracePeriodTicks;
        this.origin = origin;

        this.deliveredQuantity = 0;
        this.remainingDeadlineTicks = deadlineTicks;
        this.remainingGracePeriodTicks = gracePeriodTicks;
        this.phase = OrderPhase.OPEN;
    }

    private Order(long number,
                  ItemId product,
                  int quantity,
                  double unitPrice,
                  double urgency,
                  long deadlineTicks,
                  long gracePeriodTicks,
                  OrderOrigin origin,
                  int deliveredQuantity,
                  long remainingDeadlineTicks,
                  long remainingGracePeriodTicks,
                  OrderPhase phase) {
        this.number = number;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.urgency = urgency;
        this.deadlineTicks = deadlineTicks;
        this.gracePeriodTicks = gracePeriodTicks;
        this.origin = origin;
        this.deliveredQuantity = deliveredQuantity;
        this.remainingDeadlineTicks = remainingDeadlineTicks;
        this.remainingGracePeriodTicks = remainingGracePeriodTicks;
        this.phase = phase;
    }

    public static Order fromRequest(long number, Request request, long gracePeriodTicks, OrderOrigin origin) {
        Objects.requireNonNull(request, "request must not be null.");
        return new Order(number,
                request.getProduct(),
                request.getQuantity(),
                request.getUnitPrice(),
                request.getUrgency(),
                request.getDeadlineTicks(),
                gracePeriodTicks,
                origin);
    }

    public long getNumber() {
        return number;
    }

    public ItemId getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public int remainingQuantity() {
        return quantity - deliveredQuantity;
    }

    public boolean isFullyDelivered() {
        return deliveredQuantity >= quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getUrgency() {
        return urgency;
    }

    public long getDeadlineTicks() {
        return deadlineTicks;
    }

    public long getGracePeriodTicks() {
        return gracePeriodTicks;
    }

    public long remainingDeadlineTicks() {
        return remainingDeadlineTicks;
    }

    public long remainingGracePeriodTicks() {
        return remainingGracePeriodTicks;
    }

    public OrderPhase getPhase() {
        return phase;
    }

    public OrderOrigin getOrigin() {
        return origin;
    }

    void recordDelivery(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive.");
        if (amount > remainingQuantity()) throw new IllegalArgumentException("amount exceeds remaining quantity.");
        deliveredQuantity += amount;
    }

    void reduceRemainingDeadlineTicks(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must not be negative.");
        remainingDeadlineTicks = Math.max(0, remainingDeadlineTicks - ticks);
    }

    void reduceRemainingGracePeriodTicks(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must not be negative.");
        remainingGracePeriodTicks = Math.max(0, remainingGracePeriodTicks - ticks);
    }

    void enterGracePeriod() {
        if (phase != OrderPhase.OPEN) throw new IllegalStateException("Order is not open.");
        phase = OrderPhase.GRACE_PERIOD;
    }

    public record SaveState(
            long number,
            ItemId product,
            int quantity,
            double unitPrice,
            double urgency,
            long deadlineTicks,
            long gracePeriodTicks,
            OrderOrigin origin,
            int deliveredQuantity,
            long remainingDeadlineTicks,
            long remainingGracePeriodTicks,
            OrderPhase phase
    ) {
        public SaveState {
            Objects.requireNonNull(product, "product must not be null.");
            Objects.requireNonNull(origin, "origin must not be null.");
            Objects.requireNonNull(phase, "phase must not be null.");
        }
    }

    public SaveState getSaveState() {
        return new SaveState(number, product, quantity, unitPrice, urgency, deadlineTicks, gracePeriodTicks, origin,
                deliveredQuantity, remainingDeadlineTicks, remainingGracePeriodTicks, phase);
    }

    public static Order restore(SaveState saveState) {
        return new Order(
                saveState.number(),
                saveState.product(),
                saveState.quantity(),
                saveState.unitPrice(),
                saveState.urgency(),
                saveState.deadlineTicks(),
                saveState.gracePeriodTicks(),
                saveState.origin(),
                saveState.deliveredQuantity(),
                saveState.remainingDeadlineTicks(),
                saveState.remainingGracePeriodTicks(),
                saveState.phase()
        );
    }
}
