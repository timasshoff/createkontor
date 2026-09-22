package com.timder.kontor.core.company.financial;

import java.util.*;

public final class Account {

    private Money balance;
    private final List<Booking> bookings;

    private Account(Money balance, List<Booking> bookings) {
        this.balance = balance;
        this.bookings = bookings;
    }

    public static Account empty() {
        return new Account(Money.ZERO, new ArrayList<>());
    }

    public Money getBalance() {
        return balance;
    }

    public List<Booking> getBookings() {
        return List.copyOf(bookings);
    }

    /**
     * Books an account.
     * @param day The day of the booking
     * @param kind The kind of the booking
     * @param amount The amount
     * @param reference A reference
     * @return
     */
    public Booking book(long day, BookingKind kind, Money amount, String reference) {
        Booking booking = new Booking(day, kind, amount, reference, balance.plus(amount));
        balance = booking.balanceAfter();
        bookings.add(booking);
        return booking;
    }

    /**
     * Wheter a company can spend an amount.
     * The balance after must not fall below the overdraft limit.
     * @param cost The amount
     * @param overdraftLimit The overdraft limit
     * @return True if the balance after paying would still be above the overdraft limit.
     */
    public boolean canAfford(Money cost, Money overdraftLimit) {
        if (cost.isNegative()) throw new IllegalArgumentException("cost must not be negative.");
        if (overdraftLimit.isNegative()) throw new IllegalArgumentException("overdraftLimit must not be negative.");
        return balance.minus(cost).compareTo(overdraftLimit.negate()) >= 0;
    }

    /**
     * Pays for something and books the cost as a negative amount
     * if the overdraft limit allows it, otherwise does nothing.
     *
     * @param day The game day
     * @param kind The kind of the booking
     * @param cost What it costs, a positive amount (not zero)
     * @param reference What the booking is about, empty for nothing
     * @param overdraftLimit The overdraft limit of the legal form
     * @return True if it was paid and booked, false if the player cannot afford it (nothing changed)
     * @throws IllegalArgumentException if the cost is zero or negative, or the limit is negative
     */
    public boolean tryDebit(long day, BookingKind kind, Money cost, String reference, Money overdraftLimit) {
        if (!cost.isPositive()) throw new IllegalArgumentException("cost must be positive.");
        if (!canAfford(cost, overdraftLimit)) return false;
        book(day, kind, cost.negate(), reference);
        return true;
    }

    /**
     * Removes individual bookings that are too old. Daily sum remains the same.
     * @param today The current day
     * @param retentionDays How many days of bookings stay
     */
    public void prune(long today, int retentionDays) {
        if (retentionDays < 1) throw new IllegalArgumentException("retentionDays must be at least 1.");
        long firstKept = today - retentionDays + 1;
        bookings.removeIf(booking -> booking.day() < firstKept);
    }

    /**
     * The sum of all bookings of one day per kind
     * @param day The game day
     * @return An unmodifiable map from kind to sum
     */
    public Map<BookingKind, Money> sumsByKind(long day) {
        Map<BookingKind, Money> sums = new EnumMap<>(BookingKind.class);
        for (Booking booking : bookings) {
            if (booking.day() == day) sums.merge(booking.kind(), booking.amount(), Money::plus);
        }
        return Collections.unmodifiableMap(sums);
    }

    /**
     * The result of one day (= revenues - costs).
     * @param day The day
     * @return The result (zero, if there were no bookings)
     */
    public Money resultOfDay(long day) {
        Money result = Money.ZERO;
        for (Booking booking : bookings) {
            if (booking.day() == day && !booking.kind().isFinancing())
                result = result.plus(booking.amount());
        }
        return result;
    }

    public record SaveState(Money balance, List<Booking> bookings) {
        public SaveState {
            Objects.requireNonNull(balance, "balance must not be null.");
            bookings = List.copyOf(bookings);
        }
    }

    public SaveState getSaveState() {
        return new SaveState(balance, List.copyOf(bookings));
    }

    public static Account restore(SaveState saveState) {
        return new Account(saveState.balance(), new ArrayList<>(saveState.bookings()));
    }

    @Override
    public String toString() {
        return "Account[balance=%s, bookings=%d]".formatted(balance, bookings.size());
    }
}
