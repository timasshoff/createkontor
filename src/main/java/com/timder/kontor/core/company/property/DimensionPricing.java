package com.timder.kontor.core.company.property;

import com.timder.kontor.core.company.financial.Money;
import java.util.Objects;

public record DimensionPricing(DimensionId dimension, BiomeId referenceBiome, Money referencePrice) {
    public DimensionPricing {
        Objects.requireNonNull(dimension, "dimension must not be null.");
        Objects.requireNonNull(referenceBiome, "referenceBiome must not be null.");
        Objects.requireNonNull(referencePrice, "referencePrice must not be null.");
        if (!referencePrice.isPositive()) {
            throw new IllegalArgumentException("referencePrice must be positive.");
        }
    }
}
