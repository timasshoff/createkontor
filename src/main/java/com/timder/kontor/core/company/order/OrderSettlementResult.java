package com.timder.kontor.core.company.order;

import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

/**
 * The result of settling one order
 * @param outcome How the order ended
 * @param paidQuantity The quantity the payment was based on
 * @param revenue The amount actually booked
 */
public record OrderSettlementResult(
        OrderSettlementOutcome outcome,
        int paidQuantity,
        Money revenue
) {
    public OrderSettlementResult {
        Objects.requireNonNull(outcome, "outcome must not be null.");
        Objects.requireNonNull(revenue, "revenue must not be null.");
        if (paidQuantity < 0) throw new IllegalArgumentException("paidQuantity must not be negative.");
        if (revenue.isNegative()) throw new IllegalArgumentException("revenue must not be negative.");
    }
}
