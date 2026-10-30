package com.timder.kontor.core.company.property;

import java.util.*;

/**
 * @param rarityExponent How strongly rarity moves the price
 * @param minFactor The lowest price factor
 * @param maxFactor The highest price factor
 * @param dimensions The price anchor of every dimension
 */
public record PropertyPricingParams(
        double rarityExponent, double minFactor, double maxFactor, List<DimensionPricing> dimensions) {
    public PropertyPricingParams {
        if (!(rarityExponent >= 0.0) || Double.isInfinite(rarityExponent)) {
            throw new IllegalArgumentException("rarityExponent must be zero or positive and finite.");
        }
        if (!(minFactor > 0.0) || Double.isInfinite(minFactor)) {
            throw new IllegalArgumentException("minFactor must be positive and finite.");
        }
        if (!(maxFactor > 0.0) || Double.isInfinite(maxFactor)) {
            throw new IllegalArgumentException("maxFactor must be positive and finite.");
        }
        if (minFactor > 1.0) {
            throw new IllegalArgumentException(
                    "minFactor must not be above 1, or the reference biome would not cost the reference price.");
        }
        if (maxFactor < 1.0) {
            throw new IllegalArgumentException(
                    "maxFactor must not be below 1, or the reference biome would not cost the reference price.");
        }
        Objects.requireNonNull(dimensions, "dimensions must not be null.");
        for (DimensionPricing pricing : dimensions) {
            Objects.requireNonNull(pricing, "dimensions must not contain null.");
        }
        dimensions = List.copyOf(dimensions);

        Set<DimensionId> seen = new HashSet<>();
        for (DimensionPricing pricing : dimensions) {
            if (!seen.add(pricing.dimension())) {
                throw new IllegalArgumentException("Dimension " + pricing.dimension() + " is configured twice.");
            }
            if (!pricing.referencePrice().scaled(minFactor).isPositive()) {
                throw new IllegalArgumentException(
                        "In " + pricing.dimension()
                                + " the cheapest chunk would cost nothing: the reference price times minFactor rounds to zero cents.");
            }
            try {
                pricing.referencePrice().scaled(maxFactor);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "In " + pricing.dimension()
                                + " the most expensive chunk would not fit into a price: reference price times maxFactor is too large.");
            }
        }
    }

    public Optional<DimensionPricing> dimension(DimensionId dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null.");
        for (DimensionPricing pricing : dimensions) {
            if (pricing.dimension().equals(dimension)) {
                return Optional.of(pricing);
            }
        }
        return Optional.empty();
    }
}
