package com.timder.kontor.core.raw;

/**
 * A set of parameters for the entire raw material price process. This is used by every shared
 * material, unlike {@link RawMaterialParams}, which is unique to one material.
 *
 * @param meanReversion How strongly a price is pulled back toward its base value each day
 * @param purchasePressure How strongly a purchase pushes the price up
 * @param minFactor Hard floor relative to base value
 * @param maxFactor Hard ceiling relative to base value
 */
public record PriceProcessParams(
        double meanReversion,
        double purchasePressure,
        double minFactor,
        double maxFactor
) {

    public PriceProcessParams {
        if (meanReversion <= 0 || meanReversion > 1) throw new IllegalArgumentException("meanReversion must be within (0, 1].");
        if (purchasePressure < 0) throw new IllegalArgumentException("purchasePressure must not be negative.");
        if (minFactor <= 0) throw new IllegalArgumentException("minFactor must be positive.");
        if (maxFactor <= minFactor) throw new IllegalArgumentException("maxFactor must be larger than minFactor.");
    }

    /*
     * The following code is AI generated. Might be changed in the future.
     */

    public static PriceProcessParams standard() {
        return new PriceProcessParams(0.15, 0.10, 0.3, 4.0);
    }
}
