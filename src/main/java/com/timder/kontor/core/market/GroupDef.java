package com.timder.kontor.core.market;

public record GroupDef(
        double priceSensitivity,
        double reputationWeight,
        double targetMargin,
        double priceResponse,
        double capacityResponse,
        double cycleSensitivity
) {

    public GroupDef {
        if (priceSensitivity <= 0) throw new IllegalArgumentException("priceSensitivity must be positive.");
        if (reputationWeight < 0) throw new IllegalArgumentException("reputationWeight must not be negative.");
        if (targetMargin <= 0) throw new IllegalArgumentException("targetMargin must be positive.");
        if (priceResponse <= 0) throw new IllegalArgumentException("priceResponse must be positive.");
        if (capacityResponse <= 0) throw new IllegalArgumentException("capacityResponse must be positive.");
        if (cycleSensitivity < 0) throw new IllegalArgumentException("cycleSensitivity must not be negative.");
    }

    /*
     * The following code is AI generated. Might be changed in the future.
     */

    /** Pure price competition, sluggish demand, fast reactions. */
    public static GroupDef basicGoods() {
        return new GroupDef(4.0, 0.3, 0.10, 0.12, 0.06, 0.3);
    }

    /** Follows the business cycle strongly, boom and bust. */
    public static GroupDef building() {
        return new GroupDef(3.0, 0.5, 0.12, 0.10, 0.06, 1.2);
    }

    /** Solid middle ground. */
    public static GroupDef metal() {
        return new GroupDef(3.5, 0.5, 0.12, 0.10, 0.05, 0.8);
    }

    /** Reputation matters, competition is slow, margins are higher. */
    public static GroupDef mechanical() {
        return new GroupDef(2.5, 1.0, 0.15, 0.08, 0.04, 1.0);
    }

    /** Stable demand, reputation matters. */
    public static GroupDef food() {
        return new GroupDef(2.0, 0.8, 0.12, 0.10, 0.05, 0.2);
    }
}
