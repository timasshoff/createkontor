package com.timder.kontor.core.company.logistics;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.economy.Economy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class LogisticsStorageRules {

    public static final String BOOKING_REFERENCE = "logistics";

    /**
     * Takes a sample of the stored items
     * @param state The state
     * @param nowTick The economy tick of this sample
     * @param items The amount of items stored right now
     * @return The state with the time since the last sample added to the unbilled item-ticks and the new sample
     */
    public static LogisticsState sample(LogisticsState state, long nowTick, long items) {
        Objects.requireNonNull(state, "state must not be null.");
        if (!state.isConnected()) throw new IllegalStateException("A sample needs a connected state.");
        if (nowTick < 0) throw new IllegalArgumentException("nowTick must not be negative.");
        if (items < 0) throw new IllegalArgumentException("items must not be negative.");

        long unbilled = state.unbilledItemTicks();
        if (state.hasSample()) {
            if (nowTick < state.lastSampleTick()) {
                throw new IllegalArgumentException(
                        "nowTick " + nowTick + " is before the last sample at " + state.lastSampleTick() + ".");
            }
            long gap = Math.min(nowTick - state.lastSampleTick(), Economy.TRADING_TICK_LENGTH);
            unbilled = addSaturating(unbilled, state.lastSampleItems(), gap);
        }
        return new LogisticsState(state.networkId(), unbilled, items, nowTick);
    }

    /**
     * Sets the time since the last sample without sampling again
     * @param state The state
     * @param nowTick The current economy tick
     * @return The state with the time since the last sample added to the unbilled item ticks
     */
    public static LogisticsState closeAt(LogisticsState state, long nowTick) {
        Objects.requireNonNull(state, "state must not be null.");
        if (!state.hasSample()) {
            return state;
        }
        return sample(state, nowTick, state.lastSampleItems());
    }

    /**
     * @param itemTicks The item-ticks
     * @param params The company parameters
     * @return The storage cost of these item ticks
     */
    public static Money storageCost(long itemTicks, CompanyParams params) {
        Objects.requireNonNull(params, "params must not be null.");
        if (itemTicks < 0) throw new IllegalArgumentException("itemTicks must not be negative.");

        BigDecimal cents = BigDecimal.valueOf(itemTicks)
                .multiply(BigDecimal.valueOf(params.storageFeePerItemDay()))
                .movePointRight(2)
                .divide(BigDecimal.valueOf(Economy.DAY_LENGTH), 0, RoundingMode.HALF_UP);
        if (cents.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
            return Money.ofCents(Long.MAX_VALUE);
        }
        return Money.ofCents(cents.longValueExact());
    }

    /**
     * Books the unbilled item-ticks as one storage cost and resets them
     * @param company The company
     * @param day The day of the booking
     * @param params The company parameters
     * @return The booked cost
     */
    public static Money bookUnbilled(Company company, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        LogisticsState state = company.logistics();
        if (state.unbilledItemTicks() == 0) {
            return Money.ZERO;
        }

        Money cost = storageCost(state.unbilledItemTicks(), params);
        if (cost.isPositive()) {
            company.account().book(day, BookingKind.STORAGE, cost.negate(), BOOKING_REFERENCE);
        }
        company.setLogistics(new LogisticsState(state.networkId(), 0, state.lastSampleItems(), state.lastSampleTick()));
        return cost;
    }

    private static long addSaturating(long unbilled, long items, long gap) {
        if (items == 0 || gap == 0) {
            return unbilled;
        }
        long room = Long.MAX_VALUE - unbilled;
        if (items > room / gap) {
            return Long.MAX_VALUE;
        }
        return unbilled + items * gap;
    }
}
