package com.timder.kontor.core.company.financial;

import java.util.Objects;

public record BankLoanQuote(
        BankLoanOfferDef offer, double rate, Money firstPayment, Money totalInterest, int totalDays) {
    public BankLoanQuote {
        Objects.requireNonNull(offer, "offer must not be null.");
        Objects.requireNonNull(firstPayment, "firstPayment must not be null.");
        Objects.requireNonNull(totalInterest, "totalInterest must not be null.");
        if (!Double.isFinite(rate) || rate < 0.0)
            throw new IllegalArgumentException("rate must be finite and not negative.");
        if (!firstPayment.isPositive()) throw new IllegalArgumentException("firstPayment must be positive.");
        if (totalInterest.isNegative()) throw new IllegalArgumentException("totalInterest must not be negative.");
        if (totalDays < 1) throw new IllegalArgumentException("totalDays must be at least 1.");
    }
}
