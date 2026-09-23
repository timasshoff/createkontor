package com.timder.kontor.core.company.request;

public record QuantityStep(
        int k,
        double probability
) {
    public QuantityStep {
        if (k <= 0) throw new IllegalArgumentException("k must be positive.");
        if (probability <= 0 || probability > 1) throw new IllegalArgumentException("probability must be between 0 (exclusive) and 1.");
    }
}
