package com.timder.kontor.core.market;

public record MarketDynamicsParams(
        double deviationDecay,
        double deviationStrength,
        double deviationBound,
        double purchaseImpact,
        double maxPriceFactor,
        double minCompetitors,
        double maxCompetitors,
        double costPassThrough
) {

    public MarketDynamicsParams {
        if (deviationDecay < 0 || deviationDecay >= 1) throw new IllegalArgumentException("deviationDecay must be within [0, 1).");
        if (deviationStrength < 0) throw new IllegalArgumentException("deviationStrength must not be negative.");
        if (deviationBound <= 0 || deviationBound >= 1) throw new IllegalArgumentException("deviationBound must be within (0, 1).");
        if (purchaseImpact < 0) throw new IllegalArgumentException("purchaseImpact must not be negative.");
        if (maxPriceFactor <= 1) throw new IllegalArgumentException("maxPriceFactor must be larger than 1.");
        if (minCompetitors <= 0) throw new IllegalArgumentException("minCompetitors must be positive.");
        if (maxCompetitors < minCompetitors) throw new IllegalArgumentException("maxCompetitors must not be smaller than minCompetitors.");
        if (costPassThrough < 0 || costPassThrough > 1) throw new IllegalArgumentException("costPassThrough must be within [0, 1].");
    }

    /*
    The following code is AI generated
     */

    public static MarketDynamicsParams standard() {
        return new MarketDynamicsParams(0.94, 0.02, 0.5, 1.0, 2.0, 1.0, 8.0, 1.0);
    }
}
