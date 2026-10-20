package com.timder.kontor.core.company.financial;

import java.util.Objects;

/**
 * @param id ID of this offer
 * @param termDays Days of repayment after free days
 * @param freeDays Days without interest and repayment
 * @param termSurcharge Daily Surcharge on the interest rate
 */
public record BankLoanOfferDef(String id, int termDays, int freeDays, double termSurcharge) {

    public BankLoanOfferDef {
        Objects.requireNonNull(id, "id must not be null.");
        if (!id.matches("[a-z][a-z0-9_]*"))
            throw new IllegalArgumentException(
                    "id must be lower case letters, digits and underscores, starting with a letter.");
        if (termDays < 1) throw new IllegalArgumentException("termDays must be at least 1.");
        if (freeDays < 0) throw new IllegalArgumentException("freeDays must not be negative.");
        if (!(termSurcharge >= 0.0) || Double.isInfinite(termSurcharge))
            throw new IllegalArgumentException("termSurcharge must be a finite number, not negative.");
    }
}
