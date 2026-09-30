package com.timder.kontor.core.company.order;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.license.LicenseDef;
import com.timder.kontor.core.company.license.LicenseHoldingRules;
import com.timder.kontor.core.company.license.LicenseRules;

import java.util.List;
import java.util.Objects;

public final class OrderRules {

    /**
     * A fully delivered order before the deadline ran out.
     * Pays full revenue.
     * @param company The company to book the revenue onto.
     * @param day The current day
     * @param order The order to settle
     * @return The settlement result
     */
    public static OrderSettlementResult settleOnTime(Company company, long day, Order order, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(order, "order must not be null.");
        if (!order.isFullyDelivered()) throw new IllegalStateException("Order is not fully delivered.");
        if (order.getPhase() != OrderPhase.OPEN) throw new IllegalStateException("Order is past its deadline.");

        Money revenue = fullValue(order);
        bookIfPositive(company, day, BookingKind.ORDER_REVENUE, revenue, order);
        bookLicenseShare(company, day, order, revenue, params);
        company.recordFulfilledOrder();
        return new OrderSettlementResult(OrderSettlementOutcome.ON_TIME, order.getQuantity(), revenue);
    }

    /**
     * A fully delivered order before the grace period ran out.
     * @param company The company to book the revenue onto.
     * @param day The current day
     * @param order The order to settle
     * @return The settlement result
     */
    public static OrderSettlementResult settleLate(Company company, long day, Order order, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(order, "order must not be null.");
        if (!order.isFullyDelivered()) throw new IllegalStateException("Order is not fully delivered.");
        if (order.getPhase() != OrderPhase.GRACE_PERIOD || order.remainingGracePeriodTicks() <= 0) throw new IllegalStateException("Order is not within its grace period.");

        Money revenue = fullValue(order).scaled(0.5); // TODO make this configurable
        bookIfPositive(company, day, BookingKind.ORDER_REVENUE, revenue, order);
        bookLicenseShare(company, day, order, revenue, params);
        company.recordFulfilledOrder();
        return new OrderSettlementResult(OrderSettlementOutcome.LATE, order.getQuantity(), revenue);
    }

    /**
     * A cancelled or burst order. Pays 50% of whatever was delivered so for.
     * @param company The company to book the revenue onto.
     * @param day The current day
     * @param order The order to settle
     * @return The settlement result
     */
    public static OrderSettlementResult settleFailed(Company company, long day, Order order) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(order, "order must not be null.");

        int deliveredQuantity = order.getDeliveredQuantity();
        Money revenue = Money.fromDollar(order.getUnitPrice()).multipliedBy(deliveredQuantity).scaled(0.5);
        bookIfPositive(company, day, BookingKind.PARTIAL_PAYMENT, revenue, order);
        return new OrderSettlementResult(OrderSettlementOutcome.FAILED, deliveredQuantity, revenue);
    }

    private static void bookLicenseShare(Company company, long day, Order order, Money revenue, CompanyParams params) {
        List<LicenseDef> active = LicenseHoldingRules.activeLicenses(company, params.licenses());
        LicenseDef license = LicenseRules.cheapestApplicable(active, order.getProduct()).orElse(null);
        if (license == null) {
            return;
        }
        Money share = LicenseRules.revenueShare(license, revenue);
        if (share.isPositive()) {
            company.account().book(day, BookingKind.TURNOVER_LICENSE, share.negate(), "order_" + order.getNumber());
        }
    }

    private static Money fullValue(Order order) {
        return Money.fromDollar(order.getUnitPrice()).multipliedBy(order.getQuantity());
    }

    private static void bookIfPositive(Company company, long day, BookingKind kind, Money amount, Order order) {
        if (amount.isPositive()) {
            company.account().book(day, kind, amount, "order_" + order.getNumber());
        }
    }
}
