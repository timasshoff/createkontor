package com.timder.kontor.core.company.legalform;

import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

/**
 * What a company has to fulfil to advance into a new legal form
 * @param fee The fee paid when applying. Lost if the application is rejected
 * @param minNetWorth The net worth the company must have
 * @param minFulfilledOrders The amount of total fulfilled orders the company must have
 * @param minReputationStars The overall reputation of the company in starts that is required
 * @param processingTicks The ticks an application takes to be processed
 * @param restingDays The days an application rests if the net worth does not suffice.
 *                    The application is rejected after the rest period if the net worth is till not sufficient.
 */
public record UpgradeRequirements(
        Money fee,
        Money minNetWorth,
        int minFulfilledOrders,
        double minReputationStars,
        long processingTicks,
        int restingDays
) {

    public static final double MIN_STARS = 1.0;
    public static final double MAX_STARS = 5.0;

    public UpgradeRequirements {
        Objects.requireNonNull(fee, "fee must not be null.");
        Objects.requireNonNull(minNetWorth, "minNetWorth must not be null.");
        if (fee.isNegative()) throw new IllegalArgumentException("fee must not be negative.");
        if (minNetWorth.isNegative()) throw new IllegalArgumentException("minNetWorth must not be negative.");
        if (minFulfilledOrders < 0) throw new IllegalArgumentException("minFulfilledOrders must not be negative.");
        if (!(minReputationStars >= MIN_STARS && minReputationStars <= MAX_STARS)) {
            throw new IllegalArgumentException("minReputationStars must be between " + MIN_STARS + " and " + MAX_STARS + ".");
        }
        if (processingTicks < 1) throw new IllegalArgumentException("processingTicks must be at least 1.");
        if (restingDays < 0) throw new IllegalArgumentException("restingDays must not be negative.");
    }

    public Money moneyNeededToApply() {
        return  minNetWorth.plus(fee);
    }
}
