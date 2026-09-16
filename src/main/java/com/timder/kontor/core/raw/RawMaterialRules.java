package com.timder.kontor.core.raw;

import com.timder.kontor.core.port.Rng;

public final class RawMaterialRules {

    public static void advanceDay(RawMaterialState state, RawMaterialParams materialParams, PriceProcessParams processParams, Rng rng) {
        double base = materialParams.baseValue();
        double pullBack = processParams.meanReversion() * (Math.log(base) - Math.log(state.getPrice()));
        double noise = materialParams.dailyVolatility() * rng.nextGaussian();
        double purchasePush = processParams.purchasePressure() * state.getPurchasedToday() / materialParams.referenceVolume();

        double logPrice = Math.log(state.getPrice()) + pullBack + noise + purchasePush;
        double price = clamp(Math.exp(logPrice), processParams.minFactor() * base, processParams.maxFactor() * base);

        state.setPrice(price);
        state.clearPurchasedToday();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
