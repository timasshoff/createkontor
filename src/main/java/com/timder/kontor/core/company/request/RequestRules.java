package com.timder.kontor.core.company.request;

import com.timder.kontor.core.port.Rng;

public final class RequestRules {

    /**
     * One minecraft day in ticks.
     * Must stay in sync with {@code Economy.DAY_LENGTH}.
     */
    public static final long TICKS_PER_DAY = 24_000;

    public static double averageRequestSize(int packageSize, int maxQuantity, RequestParams params) {
        if (packageSize <= 0) throw new IllegalArgumentException("packageSize must be positive.");
        if (maxQuantity <= 0) throw new IllegalArgumentException("maxQuantity must be positive.");

        double sum = 0.0;
        for (QuantityStep step : params.quantitySteps()) {
            sum += step.probability() * quantity(step.k(), packageSize, maxQuantity);
        }
        return sum;
    }

    /**
     * The expected new requests per tick.
     * @param expectedDailyQuantity The company's expected daily quantity from the market share.
     * @param frameworkContractQuantity Daily quantity already bound by contracts
     * @param overflowShare The company's share of yesterdays market overflow
     * @param averageRequestSize The average size of one request
     * @param listPrice The current list price of the company for this product
     * @param displayedMarketPrice The markets current displayed price
     * @param params The request parameters
     * @return The arrival rate in requests per tick
     */
    public static double rate(double expectedDailyQuantity,
                              double frameworkContractQuantity,
                              double overflowShare,
                              double averageRequestSize,
                              double listPrice,
                              double displayedMarketPrice,
                              RequestParams params) {
        if (averageRequestSize <= 0) throw new IllegalArgumentException("averageRequestSize must be positive.");
        if (listPrice <= 0) throw new IllegalArgumentException("listPrice must be positive.");
        if (displayedMarketPrice <= 0) throw new IllegalArgumentException("displayedMarketPrice must be positive.");

        double effectiveQuantity = Math.max(0.0, expectedDailyQuantity - frameworkContractQuantity);
        double rate = (effectiveQuantity + overflowShare) / (averageRequestSize * TICKS_PER_DAY);

        if (listPrice <= params.walkInPriceThreshold() * displayedMarketPrice) {
            rate = Math.max(rate, params.walkInRequestsPerDay() / TICKS_PER_DAY); // TODO make this configurable
        }

        return rate;
    }

    /**
     * Draws the countdown in ticks until the next request
     * @param rate The arrival rate
     * @param rng A source of randomness
     * @return The countdown in ticks
     */
    public static long drawCountdown(double rate, Rng rng) {
        if (rate <= 0) throw new IllegalArgumentException("rate must be positive.");
        double u = 1.0 - rng.nextDouble();
        long ticks = Math.round(-Math.log(u) / rate);
        return Math.max(1, ticks);
    }

    /**
     * Draws the quantity factor from the configured distribution
     * @param params The request parameters
     * @param rng A source of randomness
     * @return The drawn quantity factor
     */
    public static int drawQuantityFactor(RequestParams params, Rng rng) {
        double u = rng.nextDouble();
        double cumulative = 0.0;
        for (QuantityStep step : params.quantitySteps()) {
            cumulative += step.probability();
            if (u < cumulative) {
                return step.k();
            }
        }
        return params.quantitySteps().get(params.quantitySteps().size() - 1).k();
    }

    /**
     * The quantity of one request
     * @param quantityFactor The quantity factor
     * @param packageSize The package size
     * @param maxQuantity The legal forms max order quantity
     * @return The requests quantity
     */
    public static int quantity(int quantityFactor, int packageSize, int maxQuantity) {
        if (quantityFactor <= 0) throw new IllegalArgumentException("quantityFactor must be positive.");
        if (packageSize <= 0) throw new IllegalArgumentException("packageSize must be positive.");
        if (maxQuantity <= 0) throw new IllegalArgumentException("maxQuantity must be positive.");
        if (maxQuantity < packageSize) throw new IllegalArgumentException("maxQuantity must be at least packageSize.");

        long raw = (long) quantityFactor * packageSize;
        long maxPackageQuantity = (long) (maxQuantity / packageSize) * packageSize;
        return (int) Math.min(raw, maxPackageQuantity);

    }

    /**
     * Draws the urgency factor
     * @param params Request parameters
     * @param rng The random source
     * @return The urgency, 0 to just under 1
     */
    public static double drawUrgency(RequestParams params, Rng rng) {
        if (rng.nextDouble() < params.urgencyProbability()) {
            return rng.nextDouble();
        }
        return 0.0;
    }

    /**
     * The manufacturing time per unit, derived from the recipe depth
     * @param depth The manufacturing depth of the product
     * @return The time in ticks per unit
     */
    public static double manufacturingTicksPerUnit(int depth) {
        if (depth < 0) throw new IllegalArgumentException("depth must not be negative.");
        return 40.0 * Math.pow(1 + depth, 1.5);
    }

    /**
     * The deadline an order would receive if the request was accepted right now
     * @param quantity The requests quantity
     * @param urgency The urgency
     * @param manufacturingTicksPerUnit The manufacturing ticks per unit
     * @param deadlineFactor The legal forms deadline factor
     * @param params Request parameters
     * @return The deadline in ticks
     */
    public static long deadlineTicks(int quantity, double urgency, double manufacturingTicksPerUnit, double deadlineFactor, RequestParams params) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive.");
        if (urgency < 0 || urgency > 1) throw new IllegalArgumentException("urgency must be between 0 and 1.");
        if (manufacturingTicksPerUnit < 0) throw new IllegalArgumentException("manufacturingTicksPerUnit must not be negative.");
        if (deadlineFactor <= 0) throw new IllegalArgumentException("deadlineFactor must be positive.");

        double raw = (params.deadlineBaseTicks() + quantity * manufacturingTicksPerUnit) * (1 - params.deadlineUrgencyFactor() * urgency) * deadlineFactor;
        return Math.round(Math.max(params.deadlineFloorTicks(), raw));
    }

    /**
     * The unit price of a request (including any surcharges and discounts)
     * @param listPrice The company's list price
     * @param urgency The urgency factor
     * @param quantityFactor The quantity factor
     * @param params Request Parameters
     * @return The unit price
     */
    public static double unitPrice(double listPrice, double urgency, int quantityFactor, RequestParams params) {
        if (listPrice <= 0) throw new IllegalArgumentException("listPrice must be positive.");
        if (urgency < 0 || urgency > 1) throw new IllegalArgumentException("urgency must be between 0 and 1.");
        if (quantityFactor <= 0) throw new IllegalArgumentException("quantityFactor must be positive.");

        double quantityDiscount = Math.max(params.quantityDiscountFloor(), 1 - params.quantityDiscountPerStep() * (quantityFactor - 1));
        return listPrice * (1 + params.priceUrgencyFactor() * urgency) * quantityDiscount;
    }

    /**
     * How long a request stays on the board before it expires unanswered.
     * @param urgency The requests urgency factor
     * @param salesRepFactor The factor of a salesman
     * @param params Request parameters
     * @return The offer duration in ticks
     */
    public static long offerDurationTicks(double urgency, double salesRepFactor, RequestParams params) {
        if (urgency < 0 || urgency > 1) throw new IllegalArgumentException("urgency must be between 0 and 1.");
        if (salesRepFactor <= 0) throw new IllegalArgumentException("salesRepFactor must be positive.");

        double raw = params.offerDurationBaseTicks() * (1 - params.offerDurationUrgencyFactor() * urgency) * salesRepFactor;
        return Math.round(raw);
    }

    /**
     * The grace period a fresh order gets
     * @param deadlineTicks The orders deadline
     * @param params The request parameters
     * @return The grace period in ticks
     */
    public static long gracePeriodTicks(long deadlineTicks, RequestParams params) {
        if (deadlineTicks <= 0) throw new IllegalArgumentException("deadlineTicks must be positive.");
        return Math.round(deadlineTicks * params.gracePeriodPortion());
    }
}
