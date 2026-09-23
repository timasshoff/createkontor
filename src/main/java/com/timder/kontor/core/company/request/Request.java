package com.timder.kontor.core.company.request;

import com.timder.kontor.core.value.ItemId;

import java.util.Objects;

/**
 * An open request sitting on the board.
 * Immutable except for the time it stays on the board.
 */
public final class Request {
    private final long number;
    private final ItemId product;
    private final int quantity;
    private final int quantityFactor;
    private final double urgency;
    private final double unitPrice;
    private final long deadlineTicks;

    private long remainingOfferTicks;

    public Request(long number, ItemId product, int quantity, int quantityFactor, double urgency, double unitPrice, long deadlineTicks, long offerDurationTicks) {
        Objects.requireNonNull(product, "product must not be null.");
        if (number < 0) throw new IllegalArgumentException("number must not be negative.");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive.");
        if (quantityFactor <= 0) throw new IllegalArgumentException("quantityFactor must be positive.");
        if (urgency < 0.0 || urgency > 1.0) throw new IllegalArgumentException("urgency must be between 0 and 1.");
        if (unitPrice <= 0) throw new IllegalArgumentException("unitPrice must be positive.");
        if (deadlineTicks <= 0) throw new IllegalArgumentException("deadlineTicks must be positive.");
        if (offerDurationTicks <= 0) throw new IllegalArgumentException("offerDurationTicks must be positive.");

        this.number = number;
        this.product = product;
        this.quantity = quantity;
        this.quantityFactor = quantityFactor;
        this.urgency = urgency;
        this.unitPrice = unitPrice;
        this.deadlineTicks = deadlineTicks;
        this.remainingOfferTicks = offerDurationTicks;
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

    public int getQuantityFactor() {
        return quantityFactor;
    }

    public double getUrgency() {
        return urgency;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * The deadline an order gets if the request is accepted
     * @return The deadline in ticks
     */
    public long getDeadlineTicks() {
        return deadlineTicks;
    }

    /**
     * @return How long this offer will still stay on the board
     */
    public long remainingOfferTicks() {
        return remainingOfferTicks;
    }

    public boolean hasExpired() {
        return remainingOfferTicks <= 0;
    }

    void advanceOfferTime(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must not be negative.");
        remainingOfferTicks = Math.max(0, remainingOfferTicks - ticks);
    }

    public record SaveState(
            long number,
            ItemId product,
            int quantity,
            int quantityFactor,
            double urgency,
            double unitPrice,
            long deadlineTicks,
            long remainingOfferTicks
    ) {
        public SaveState {
            Objects.requireNonNull(product, "product must not be null.");
        }
    }

    public SaveState getSaveState() {
        return new SaveState(number, product, quantity, quantityFactor, urgency, unitPrice, deadlineTicks, remainingOfferTicks);
    }

    public static Request restore(SaveState saveState) {
        return new Request(
                saveState.number(),
                saveState.product(),
                saveState.quantity(),
                saveState.quantityFactor(),
                saveState.urgency(),
                saveState.unitPrice(),
                saveState.deadlineTicks(),
                saveState.remainingOfferTicks()
        );
    }
}
