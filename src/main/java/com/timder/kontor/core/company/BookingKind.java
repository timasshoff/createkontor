package com.timder.kontor.core.company;

import java.util.Objects;

public enum BookingKind {

    /** Business license, fixed, daily. */
    BUSINESS_LICENSE(BookingCategory.COST),
    /** Flat product license, fixed, daily. */
    FLAT_LICENSE(BookingCategory.COST),
    /** Turnover license: base fee per day plus a share of the revenue. */
    TURNOVER_LICENSE(BookingCategory.COST),
    /** Application fee, once per application. */
    APPLICATION_FEE(BookingCategory.COST),
    /** Feed in license, fixed, daily. */
    FEED_IN_LICENSE(BookingCategory.COST),
    /** Salaries. */
    SALARY(BookingCategory.COST),
    /** Power and work price together. */
    POWER(BookingCategory.COST),
    /** Storage cost. */
    STORAGE(BookingCategory.COST),
    /** Purchase of products */
    PURCHASE(BookingCategory.COST),
    /** Interest cost. */
    INTEREST(BookingCategory.COST),
    /** Penalty of a contract */
    CONTRACT_PENALTY(BookingCategory.COST),
    /** Fee of upgrading to a higher legal form */
    UPGRADE_FEE(BookingCategory.COST),

    /** Revenue from full-filled orders. */
    ORDER_REVENUE(BookingCategory.REVENUE),
    /** Partial payment of an order that burst. */
    PARTIAL_PAYMENT(BookingCategory.REVENUE),
    /** Feed in revenue */
    FEED_IN_REVENUE(BookingCategory.REVENUE),

    /** The initial deposit of a new company */
    DEPOSIT(BookingCategory.FINANCING),
    /** Payout of a loan. */
    LOAN_PAYOUT(BookingCategory.FINANCING),
    /** Repayment of a loan. */
    LOAN_REPAYMENT(BookingCategory.FINANCING);

    private final BookingCategory category;

    BookingKind(BookingCategory category) {
        this.category = Objects.requireNonNull(category);
    }

    public BookingCategory category() {
        return category;
    }

    public boolean isCost() {
        return category == BookingCategory.COST;
    }

    public boolean isRevenue() {
        return category == BookingCategory.REVENUE;
    }

    public boolean isFinancing() {
        return category == BookingCategory.FINANCING;
    }
}
