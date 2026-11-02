package com.timder.kontor.core.company.property;

import com.timder.kontor.core.company.financial.Money;
import java.util.*;

public final class PropertyPricingRules {

    public static boolean isPriceable(
            PropertyPricingParams params, DimensionId dimension, BiomeFrequencies frequencies) {
        Objects.requireNonNull(params, "params must not be null.");
        Objects.requireNonNull(dimension, "dimension must not be null.");
        Objects.requireNonNull(frequencies, "frequencies must not be null.");

        return params.dimension(dimension)
                .map(pricing -> frequencies.count(pricing.referenceBiome()) > 0)
                .orElse(false);
    }

    /**
     * @param params The pricing parameters
     * @param referenceSamples The amount of sample hits of the reference biome
     * @param biomeSamples The amount of sample hits of the biome to be priced
     * @return The price factor
     */
    public static double rarityFactor(PropertyPricingParams params, long referenceSamples, long biomeSamples) {
        Objects.requireNonNull(params, "params must not be null.");
        if (referenceSamples < 1) throw new IllegalArgumentException("referenceSamples must be at least 1.");
        if (biomeSamples < 0) throw new IllegalArgumentException("biomeSamples must not be negative.");

        double ratio = biomeSamples == 0 ? Double.POSITIVE_INFINITY : (double) referenceSamples / (double) biomeSamples;
        double raw = StrictMath.pow(ratio, params.rarityExponent());
        return Math.max(params.minFactor(), Math.min(params.maxFactor(), raw));
    }

    public static Money priceOf(
            PropertyPricingParams params, DimensionId dimension, BiomeFrequencies frequencies, BiomeId biome) {
        Objects.requireNonNull(params, "params must not be null.");
        Objects.requireNonNull(dimension, "dimension must not be null.");
        Objects.requireNonNull(frequencies, "frequencies must not be null.");
        Objects.requireNonNull(biome, "biome must not be null.");

        DimensionPricing pricing = requirePriceable(params, dimension, frequencies);
        return priceWith(params, pricing, frequencies, biome);
    }

    public static Map<BiomeId, Money> pricingMap(
            PropertyPricingParams params, DimensionId dimension, BiomeFrequencies frequencies) {
        Objects.requireNonNull(params, "params must not be null.");
        Objects.requireNonNull(dimension, "dimension must not be null.");
        Objects.requireNonNull(frequencies, "frequencies must not be null.");

        DimensionPricing pricing = requirePriceable(params, dimension, frequencies);
        List<Map.Entry<BiomeId, Money>> entries = new ArrayList<>();
        for (BiomeId biome : frequencies.samples().keySet()) {
            entries.add(Map.entry(biome, priceWith(params, pricing, frequencies, biome)));
        }

        entries.sort(Map.Entry.<BiomeId, Money>comparingByValue()
                .thenComparing(entry -> entry.getKey().value()));

        Map<BiomeId, Money> map = new LinkedHashMap<>();
        for (Map.Entry<BiomeId, Money> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    private static DimensionPricing requirePriceable(
            PropertyPricingParams params, DimensionId dimension, BiomeFrequencies frequencies) {
        DimensionPricing pricing = params.dimension(dimension)
                .orElseThrow(() ->
                        new IllegalArgumentException("Dimension " + dimension + " is not configured for property."));
        if (frequencies.count(pricing.referenceBiome()) == 0) {
            throw new IllegalStateException("The reference biome " + pricing.referenceBiome() + " has no samples in "
                    + dimension + ", so its chunks cannot be priced.");
        }
        return pricing;
    }

    private static Money priceWith(
            PropertyPricingParams params, DimensionPricing pricing, BiomeFrequencies frequencies, BiomeId biome) {
        double factor = rarityFactor(params, frequencies.count(pricing.referenceBiome()), frequencies.count(biome));
        return pricing.referencePrice().scaled(factor);
    }
}
