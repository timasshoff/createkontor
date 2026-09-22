package com.timder.kontor.core.market;

/**
 * Immutable parameters of a singular market. These are not owned by the market but rather provided with every calculation.
 * These parameters are coming from both the product group and the product itself.
 * @param referenceCost Cost per unit of the competitors
 * @param plantSize The production capacity of a competitor
 * @param targetUtilisation The utilisation a competitor wants to achieve
 * @param group The product group this market belongs to
 */
public record MarketParams (
        double referenceCost,
        double plantSize,
        double targetUtilisation,
        GroupDef group
) {

    /**
     * A reputation that counts as neutral.
     */
    public static final double NEUTRAL_REPUTATION = 3.0;

    public MarketParams {
        if (referenceCost <= 0) throw new IllegalArgumentException("referenceCost must be positive.");
        if (plantSize <= 0) throw new IllegalArgumentException("plantSize must be positive.");
        if (targetUtilisation <= 0 || targetUtilisation > 1) throw new IllegalArgumentException("targetUtilisation must be within (0, 1].");
        if (group == null) throw new IllegalArgumentException("group must not be null.");
    }

    public double priceSensitivity() {
        return group.priceSensitivity();
    }

    public double reputationWeight() {
        return group.reputationWeight();
    }

    public double targetMargin() {
        return group.targetMargin();
    }

    public double priceResponse() {
        return group.priceResponse();
    }

    public double capacityResponse() {
        return group.capacityResponse();
    }

    public double cycleSensitivity() {
        return group.cycleSensitivity();
    }

    /**
     * The price the market will settle on without any competitors.
     * @return The equilibrium price
     */
    public double equilibriumPrice() {
        return referenceCost * (1.0 + targetMargin());
    }

    /**
     * Amount of competitors the market will settle on with the given demand.
     * @param demand The demand
     * @return The equilibrium demand
     */
    public double equilibriumCompanies(double demand) {
        return demand / (targetUtilisation * plantSize);
    }

    /**
     * The target profitability of any competitor
     * @return The target profitability
     */
    public double targetProfitability() {
        return targetUtilisation * targetMargin();
    }
}
