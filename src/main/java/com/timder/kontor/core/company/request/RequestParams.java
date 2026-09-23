package com.timder.kontor.core.company.request;

import java.util.List;
import java.util.Objects;

/**
 * A set of immutable global parameters for the request system
 * @param quantitySteps With which probability a certain quantity factor is getting pulled
 * @param urgencyProbability Probability that a request is considered urgent
 * @param deadlineUrgencyFactor A factor influencing the deadline of an urgent request
 * @param priceUrgencyFactor A factor influencing the price of an urgent request
 * @param offerDurationUrgencyFactor A factor influencing the offer duration of an urgent request
 * @param deadlineBaseTicks A base deadline in ticks
 * @param deadlineFloorTicks A minimal deadline in ticks
 * @param offerDurationBaseTicks Baseline how long a request stays on the board
 * @param gracePeriodPortion Portion of the deadline that is granted as grace period
 * @param quantityDiscountFloor Floor of the quantity discount
 * @param quantityDiscountPerStep Quantity discount per step
 * @param walkInPriceThreshold The threshold relative to the market price at which at least one request per day is guaranteed
 */
public record RequestParams(
        List<QuantityStep> quantitySteps,
        double urgencyProbability,
        double deadlineUrgencyFactor,
        double priceUrgencyFactor,
        double offerDurationUrgencyFactor,
        long deadlineBaseTicks,
        long deadlineFloorTicks,
        long offerDurationBaseTicks,
        double gracePeriodPortion,
        double quantityDiscountFloor,
        double quantityDiscountPerStep,
        double walkInPriceThreshold
) {
    public RequestParams {
        Objects.requireNonNull(quantitySteps, "quantitySteps must not be null.");
        quantitySteps = List.copyOf(quantitySteps);
        if (quantitySteps.isEmpty()) throw new IllegalArgumentException("quantitySteps must not be empty.");

        double sum = quantitySteps.stream().mapToDouble(QuantityStep::probability).sum();
        if (Math.abs(sum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("quantitySteps probabilities must sum to 1.0, but summed to " + sum + ".");
        }

        if (urgencyProbability < 0 || urgencyProbability > 1) throw new IllegalArgumentException("urgencyProbability must be between 0 and 1.");
        if (deadlineUrgencyFactor < 0) throw new IllegalArgumentException("deadlineUrgencyFactor must not be negative.");
        if (priceUrgencyFactor < 0) throw new IllegalArgumentException("priceUrgencyFactor must not be negative.");
        if (offerDurationUrgencyFactor < 0) throw new IllegalArgumentException("offerDurationUrgencyFactor must not be negative.");
        if (deadlineBaseTicks <= 0) throw new IllegalArgumentException("deadlineBaseTicks must be positive.");
        if (deadlineFloorTicks <= 0) throw new IllegalArgumentException("deadlineFloorTicks must be positive.");
        if (offerDurationBaseTicks <= 0) throw new IllegalArgumentException("offerDurationBaseTicks must be positive.");
        if (gracePeriodPortion <= 0 || gracePeriodPortion >= 1) throw new IllegalArgumentException("gracePeriodPortion must be between 0 and 1 (exclusive).");
        if (quantityDiscountFloor <= 0 || quantityDiscountFloor > 1) throw new IllegalArgumentException("quantityDiscountFloor must be between 0 (exclusive) and 1.");
        if (quantityDiscountPerStep < 0) throw new IllegalArgumentException("quantityDiscountPerStep must not be negative.");
        if (walkInPriceThreshold <= 1.0) throw new IllegalArgumentException("walkInPriceThreshold must be greater than 1.0.");
    }

    /*
    The following code is AI generated
     */

    public static RequestParams standard() {
        return new RequestParams(
                List.of(
                        new QuantityStep(1, 0.30),
                        new QuantityStep(2, 0.25),
                        new QuantityStep(3, 0.15),
                        new QuantityStep(4, 0.10),
                        new QuantityStep(6, 0.08),
                        new QuantityStep(8, 0.06),
                        new QuantityStep(12, 0.04),
                        new QuantityStep(16, 0.02)
                ),
                0.25,   // urgency probability
                0.6,    // deadline urgency factor
                0.3,    // price urgency factor
                0.5,    // offer duration urgency factor
                3000,   // deadline base ticks
                2400,   // deadline floor ticks
                6000,   // offer duration base ticks
                0.25,   // grace period portion
                0.95,   // quantity discount floor
                0.005,  // quantity discount per step
                1.2     // walk-in price threshold
        );
    }
}
