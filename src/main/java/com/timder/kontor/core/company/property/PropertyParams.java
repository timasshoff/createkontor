package com.timder.kontor.core.company.property;

import com.timder.kontor.core.company.financial.Money;
import java.util.*;

/**
 * @param pricing The pricing parameters
 * @param frequencies The measured biome frequencies
 * @param dailyRentRate The daily rent as a fraction of the price
 * @param resaleShare The share of the price that a company gets when it sells a property
 */
public record PropertyParams(
        PropertyPricingParams pricing,
        Map<DimensionId, BiomeFrequencies> frequencies,
        double dailyRentRate,
        double resaleShare) {
    public PropertyParams {
        Objects.requireNonNull(pricing, "pricing must not be null.");
        Objects.requireNonNull(frequencies, "frequencies must not be null.");
        Map<DimensionId, BiomeFrequencies> copy = new LinkedHashMap<>();
        for (Map.Entry<DimensionId, BiomeFrequencies> entry : frequencies.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "dimension must not be null.");
            Objects.requireNonNull(entry.getValue(), "The frequencies of " + entry.getKey() + " must not be null.");
            copy.put(entry.getKey(), entry.getValue());
        }
        frequencies = Map.copyOf(copy);
        if (!(dailyRentRate >= 0.0) || dailyRentRate > 1.0) {
            throw new IllegalArgumentException("dailyRentRate must be between 0 and 1.");
        }
        if (!(resaleShare >= 0.0) || resaleShare > 1.0) {
            throw new IllegalArgumentException("resaleShare must be between 0 and 1.");
        }
    }

    public static PropertyParams disabled() {
        return new PropertyParams(new PropertyPricingParams(0.5, 0.2, 50.0, List.of()), Map.of(), 0.01, 0.6);
    }

    public BiomeFrequencies frequenciesOf(DimensionId dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null.");
        return frequencies.getOrDefault(dimension, BiomeFrequencies.EMPTY);
    }

    public Money dailyRent(Money price) {
        return price.scaled(dailyRentRate);
    }

    public Money resaleValue(Money price) {
        return price.scaled(resaleShare);
    }
}
