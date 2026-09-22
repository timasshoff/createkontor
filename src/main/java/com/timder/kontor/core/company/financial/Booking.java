package com.timder.kontor.core.company.financial;

import java.util.Objects;

/**
 * One booking.
 *
 * @param day The day of the booking
 * @param kind The kind of the booking
 * @param amount The amount, positive or negative. Never zero.
 * @param reference Description of the booking
 * @param balanceAfter The balance of the account right after this booking.
 */
public record Booking(
        long day,
        BookingKind kind,
        Money amount,
        String reference,
        Money balanceAfter
) {
    public Booking {
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        Objects.requireNonNull(kind, "kind must not be null.");
        Objects.requireNonNull(amount, "amount must not be null.");
        Objects.requireNonNull(reference, "reference must not be null.");
        Objects.requireNonNull(balanceAfter, "balanceAfter must not be null.");
        if (amount.isZero()) throw new IllegalArgumentException("amount must not be zero.");
    }
}
