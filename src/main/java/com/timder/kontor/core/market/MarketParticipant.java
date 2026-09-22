package com.timder.kontor.core.market;

import com.timder.kontor.core.company.CompanyId;

public record MarketParticipant(
        CompanyId companyId,
        double listPrice,
        double reputation
) {
    public MarketParticipant {
        if (companyId == null) throw new IllegalArgumentException("companyId must not be null.");
        if (listPrice <= 0) throw new IllegalArgumentException("listPrice must be positive.");
        reputation = MarketRules.clamp(reputation, 0.0, 100.0);
    }

    public double reputationInStars() {
        return MarketRules.starsFromReputation(reputation);
    }
}
