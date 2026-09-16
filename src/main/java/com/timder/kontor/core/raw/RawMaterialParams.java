package com.timder.kontor.core.raw;

/**
 * Immutable parameters of one raw material.
 * Raw Materials do not have simulated producers and instead wander around
 * a base value.
 *
 * @param baseValue The base price the material keeps drifting back towards
 * @param dailyVolatility How much the price moves daily
 * @param referenceVolume The daily quantity at which purchases start pushing the price up
 */
public record RawMaterialParams(
        double baseValue,
        double dailyVolatility,
        double referenceVolume
) {

    public RawMaterialParams {
        if (baseValue <= 0) throw new IllegalArgumentException("baseValue must be positive.");
        if (dailyVolatility < 0) throw new IllegalArgumentException("dailyVolatility must be positive");
        if (referenceVolume <= 0) throw new IllegalArgumentException("referenceVolume must be positive.");
    }

}
