package com.timder.kontor.core.company.power;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.economy.Economy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class PowerRules {

    public static void add(Company company, PowerKind kind, long suTicks) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(kind, "kind must not be null.");
        if (suTicks < 0) throw new IllegalArgumentException("suTicks must not be negative.");
        if (suTicks == 0) {
            return;
        }
        company.setPower(company.power().with(kind, suTicks));
    }

    public static Money cost(long suTicks, PowerKind kind, CompanyParams params) {
        Objects.requireNonNull(kind, "kind must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        return cost(suTicks, kind, params.power());
    }

    public static Money cost(long suTicks, PowerKind kind, PowerParams params) {
        Objects.requireNonNull(kind, "kind must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        if (suTicks < 0) throw new IllegalArgumentException("suTicks must not be negative.");

        BigDecimal cents = BigDecimal.valueOf(suTicks)
                .multiply(BigDecimal.valueOf(params.pricePerSuDay()))
                .multiply(BigDecimal.valueOf(params.factorOf(kind)))
                .movePointRight(2)
                .divide(BigDecimal.valueOf(Economy.DAY_LENGTH), 0, RoundingMode.HALF_UP);
        if (cents.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
            return Money.ofCents(Long.MAX_VALUE);
        }
        return Money.ofCents(cents.longValueExact());
    }

    public static Money bookUnbilled(Company company, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        Money total = Money.ZERO;
        for (PowerKind kind : PowerKind.values()) {
            PowerState state = company.power();
            long suTicks = state.suTicks(kind);
            if (suTicks == 0) {
                continue;
            }
            Money cost = cost(suTicks, kind, params.power());
            if (!cost.isPositive()) {
                continue;
            }
            company.account().book(day, BookingKind.POWER, cost.negate(), kind.bookingReference());
            company.setPower(state.without(kind));
            total = total.plus(cost);
        }
        return total;
    }
}
