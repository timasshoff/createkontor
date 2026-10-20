package com.timder.kontor.core.company.financial;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param offers The offers
 * @param graceDaySurcharge The daily surcharge on the interest day for every free day
 * @param riskSurchargeAtOneStar The daily surcharge on the interest day for a company with a low reputation
 * @param riskSurchargeAtFiveStars The daily surcharge on the interest day for a company with a high reputation
 * @param maxLoans The max loans a company can have at the same time
 * @param creditLimitRevenueMultiple The limit ist at most this many times the average daily revenue
 * @param creditLimitRevenueDays How many settled days are used for the calculation of the average daily revenue for the credit limit
 * @param minimumAmountShare The smallest loan is this share of the bank loan limit of the legal form
 */
public record BankLoanParams(
        List<BankLoanOfferDef> offers,
        double graceDaySurcharge,
        double riskSurchargeAtOneStar,
        double riskSurchargeAtFiveStars,
        int maxLoans,
        double creditLimitRevenueMultiple,
        int creditLimitRevenueDays,
        double minimumAmountShare) {
    public BankLoanParams {
        Objects.requireNonNull(offers, "offers must not be null.");
        offers = List.copyOf(offers);
        Set<String> ids = new HashSet<>();
        for (BankLoanOfferDef offer : offers) {
            if (!ids.add(offer.id()))
                throw new IllegalArgumentException("offers contains the id " + offer.id() + " twice.");
        }
        if (!(graceDaySurcharge >= 0.0) || Double.isInfinite(graceDaySurcharge))
            throw new IllegalArgumentException("graceDaySurcharge must be a finite number, not negative.");
        if (!(riskSurchargeAtFiveStars >= 0.0) || Double.isInfinite(riskSurchargeAtFiveStars))
            throw new IllegalArgumentException("riskSurchargeAtFiveStars must be a finite number, not negative.");
        if (!(riskSurchargeAtOneStar >= riskSurchargeAtFiveStars) || Double.isInfinite(riskSurchargeAtOneStar))
            throw new IllegalArgumentException(
                    "riskSurchargeAtOneStar must be finite and not below riskSurchargeAtFiveStars.");
        if (maxLoans < 1) throw new IllegalArgumentException("maxLoans must be at least 1.");
        if (!(creditLimitRevenueMultiple > 0.0) || Double.isInfinite(creditLimitRevenueMultiple))
            throw new IllegalArgumentException("creditLimitRevenueMultiple must be positive and finite.");
        if (creditLimitRevenueDays < 1)
            throw new IllegalArgumentException("creditLimitRevenueDays must be at least 1.");
        if (!(minimumAmountShare >= 0.0 && minimumAmountShare <= 1.0))
            throw new IllegalArgumentException("minimumAmountShare must be between 0 and 1.");
    }

    /*
    The following code is AI generated
     */

    public static BankLoanParams standard() {
        return new BankLoanParams(
                List.of(
                        new BankLoanOfferDef("short", 14, 0, 0.0),
                        new BankLoanOfferDef("grace", 28, 7, 0.0002),
                        new BankLoanOfferDef("long", 56, 0, 0.0005)),
                0.0001, // 0.01 percentage points per free day
                0.0040, // 0.40 % at one star
                0.0010, // 0.10 % at five stars
                3,
                10.0,
                7,
                0.05);
    }
}
