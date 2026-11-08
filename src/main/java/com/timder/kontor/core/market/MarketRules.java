package com.timder.kontor.core.market;

public final class MarketRules {

    public static final int TRADING_TICKS_PER_DAY = 12;
    public static final double SHARE_LIMIT_SHARPNESS = 4.0;

    /**
     * How appealing a supplier is to customers.
     * Built from reputation and price. Both factors are relative to a neutral point.
     * A supplier with a neutral reputation asking the exact market price scores 1.0.
     * A higher price sensitivity means customers chase the cheapest offer, a high reputation weight means
     * customers pay for a good name.
     * Without trust penalty.
     *
     * @param price The price per unit the supplier asks for
     * @param reputation The reputation of the supplier
     * @param state The current state of the market
     * @param params The parameters of the market
     * @return The attractiveness of that supplier. 1.0 means neutral.
     */
    public static double attractiveness(double price, double reputation, MarketState state, MarketParams params) {
        return attractiveness(price, reputation, state, params, MarketDynamicsParams.standard());
    }

    /**
     * How appealing a supplier is to customers.
     * Built from reputation and price. Both factors are relative to a neutral point.
     * A supplier with a neutral reputation asking the exact market price scores 1.0.
     * A higher price sensitivity means customers chase the cheapest offer, a high reputation weight means
     * customers pay for a good name.
     * With trust penalty.
     *
     * @param price The price per unit the supplier asks for
     * @param reputation The reputation of the supplier
     * @param state The current state of the market
     * @param params The dynamic parameters of the markets
     * @return The attractiveness of that supplier. 1.0 means neutral.
     */
    public static double attractiveness(
            double price, double reputation, MarketState state, MarketParams params, MarketDynamicsParams dynamics) {
        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive.");
        }

        double priceRatio = price / state.getDisplayedPrice();
        double reputationFactor = Math.pow(reputation / MarketParams.NEUTRAL_REPUTATION, params.reputationWeight());
        double priceFactor = Math.pow(priceRatio, -params.priceSensitivity());

        return reputationFactor * priceFactor * trustFactor(priceRatio, dynamics);
    }

    /**
     * How many customers trust an offer.
     * @param priceRatio Price of the supplier divided by market price
     * @param dynamics The dynamic parameters of the markets
     * @return The trust factor
     */
    public static double trustFactor(double priceRatio, MarketDynamicsParams dynamics) {
        double threshold = dynamics.trustThreshold();
        if (threshold <= 0 || priceRatio >= threshold) {
            return 1.0;
        }
        double distance = (threshold - priceRatio) / dynamics.trustWidth();
        return Math.pow(0.5, distance * distance);
    }

    /**
     * Limits the share of a single company.
     * @param share The share someone would get without the limit
     * @param dynamics The dynamic parameters of the markets
     * @return The share after the limit
     */
    public static double limitShare(double share, MarketDynamicsParams dynamics) {
        double limit = dynamics.maxShare();
        if (share <= 0) {
            return 0.0;
        }
        if (limit >= 1.0) {
            return share;
        }
        return Math.pow(
                Math.pow(share, -SHARE_LIMIT_SHARPNESS) + Math.pow(limit, -SHARE_LIMIT_SHARPNESS),
                -1.0 / SHARE_LIMIT_SHARPNESS);
    }

    /**
     * The attractiveness of the whole competition.
     * The competition always asks for the exact market price (price factor = 1).
     * What remains is the number of competitors, weighted by their reputation.
     * Every competing company therefore counts as much as one average supplier.
     *
     * @param state The current state of the market
     * @param params The parameters of the market
     * @return The attractiveness of all competitors combined
     */
    public static double competitorAttractiveness(MarketState state, MarketParams params) {
        double reputationFactor =
                Math.pow(state.getCompetitorReputation() / MarketParams.NEUTRAL_REPUTATION, params.reputationWeight());
        return state.getCompetitors() * reputationFactor;
    }

    /**
     * The share the competition keeps for itself.
     *
     * @param companiesAttractiveness Sum over all player competitors in this market
     * @param state The current state of the market
     * @param params The parameters of the market
     * @return The share of the daily demand the competition receives
     */
    public static double competitorShare(double companiesAttractiveness, MarketState state, MarketParams params) {
        double competitor = competitorAttractiveness(state, params);
        double total = companiesAttractiveness + competitor;
        return total > 0 ? competitor / total : 1.0;
    }

    /**
     * Splits the demand between one supplier and the rest (competition + other competitors).
     *
     * @param ownAttractiveness Attractiveness of the supplier
     * @param otherCompaniesAttractiveness Attractiveness of other player competitors (singleplayer = 0)
     * @param state The current state of the market
     * @param params The parameters of the market
     * @return The share of the daily demand this supplier receives
     */
    public static double shareFrom(
            double ownAttractiveness, double otherCompaniesAttractiveness, MarketState state, MarketParams params) {
        return shareFrom(
                ownAttractiveness, otherCompaniesAttractiveness, state, params, MarketDynamicsParams.standard());
    }

    public static double shareFrom(
            double ownAttractiveness,
            double otherCompaniesAttractiveness,
            MarketState state,
            MarketParams params,
            MarketDynamicsParams dynamics) {
        double companies = ownAttractiveness + otherCompaniesAttractiveness;
        double total = companies + competitorAttractiveness(state, params);
        if (total <= 0 || companies <= 0) {
            return 0.0;
        }
        return ownAttractiveness / companies * limitShare(companies / total, dynamics);
    }

    /**
     * Splits the daily demand into a share breakdown.
     * Shows the share of own company, other companies and competitors.
     *
     * @param ownAttractiveness Attractiveness of the company
     * @param otherCompaniesAttractiveness Summed attractiveness of all other player companies
     * @param state The current state of the market
     * @param params The parameters of the market
     * @return The three shares, adding up to one
     */
    public static MarketShareBreakdown shareBreakdown(
            double ownAttractiveness,
            double otherCompaniesAttractiveness,
            MarketState state,
            MarketParams params,
            MarketDynamicsParams dynamics) {
        double own = shareFrom(ownAttractiveness, otherCompaniesAttractiveness, state, params, dynamics);
        double others = shareFrom(otherCompaniesAttractiveness, ownAttractiveness, state, params, dynamics);
        double competition = Math.max(0.0, 1.0 - own - others);
        return new MarketShareBreakdown(own, others, competition);
    }

    public static double share(double price, double reputation, MarketState state, MarketParams params) {
        return share(price, reputation, state, params, MarketDynamicsParams.standard());
    }

    public static double share(
            double price, double reputation, MarketState state, MarketParams params, MarketDynamicsParams dynamics) {
        return shareFrom(attractiveness(price, reputation, state, params, dynamics), 0.0, state, params, dynamics);
    }

    public static void advanceTradingTick(
            MarketState state, double actualDelivered, double expectedDelivered, double demandThisTick) {
        advanceTradingTick(
                state, actualDelivered, 0.0, expectedDelivered, demandThisTick, MarketDynamicsParams.standard());
    }

    /**
     * Advanced the trading tick forward. Moves the trading deviation.
     * @param state The market state
     * @param actualDelivered The amount of actually delivered product across the market
     * @param actualPurchased The amount of product that companies bought from the competitors in this tick
     * @param expectedDelivered The expected amount of delivered product by all competitors and concurrence, based on their market shared
     * @param demandThisTick The demand for this tick
     */
    public static void advanceTradingTick(
            MarketState state,
            double actualDelivered,
            double actualPurchased,
            double expectedDelivered,
            double demandThisTick,
            MarketDynamicsParams params) {
        double surprise = actualDelivered - expectedDelivered - actualPurchased;
        double reference = demandThisTick > 0 ? demandThisTick : 1.0;

        double deviation =
                params.deviationDecay() * state.getDeviation() - params.deviationStrength() * surprise / reference;

        state.setDeviation(clamp(deviation, -params.deviationBound(), params.deviationBound()));
        state.clearDeliveredThisTick();
        state.clearPurchasedThisTick();
    }

    /**
     * Passes a change of the reference cost on to the price level
     * @param state The market state
     * @param previousCost The reference cost before recalculation
     * @param newCost The reference cost after recalculation
     * @param dynamics The dynamic market parameters
     */
    public static void passCostChange(
            MarketState state, double previousCost, double newCost, MarketDynamicsParams dynamics) {
        if (previousCost <= 0 || newCost <= 0 || newCost == previousCost) {
            return;
        }
        double ratio = newCost / previousCost;
        double passThrough = ratio > 1.0 ? dynamics.costPassThrough() : dynamics.costPassThroughDown();
        state.setPriceLevel(state.getPriceLevel() * (1.0 + passThrough * (ratio - 1.0)));
    }

    public static DayResult advanceDay(MarketState state, MarketParams params, double demand) {
        return advanceDay(state, params, demand, MarketDynamicsParams.standard());
    }

    public static DayResult advanceDay(
            MarketState state, MarketParams params, double demand, MarketDynamicsParams dynamicsParams) {
        double priceBefore = state.getPriceLevel();
        double competitorsBefore = state.getCompetitors();

        // The capacity of the competitors
        double capacity = state.getCapacity(params);

        // The remaining demand
        double soldByCompanies = Math.min(state.getDeliveredToday(), demand);
        double remaining = Math.max(0.0, demand - soldByCompanies) + state.getPurchasedToday();

        // The performance of competitors
        double soldByCompetitors = Math.min(remaining, capacity);
        double overflow = remaining - soldByCompetitors;

        // Utilisation
        double utilisation = capacity > 0 ? soldByCompetitors / capacity : 0.0;

        // Adapt price level
        double price = priceBefore * (1.0 + params.priceResponse() * (utilisation - params.targetUtilisation()));
        price = clamp(price, params.referenceCost(), params.referenceCost() * dynamicsParams.maxPriceFactor());
        state.setPriceLevel(price);

        // Profitability
        double margin = (price - params.referenceCost()) / params.referenceCost();
        double profitability = utilisation * margin;
        double target = params.targetProfitability();
        double relative = clamp((profitability - target) / target, -1.0, 1.0);

        // Adapt amount of competitors
        double competitors = clamp(
                competitorsBefore * (1.0 + params.capacityResponse() * relative),
                dynamicsParams.minCompetitors(),
                dynamicsParams.maxCompetitors());
        state.setCompetitors(competitors);

        // Clearing counters
        state.clearDeliveredToday();
        state.clearPurchasedToday();

        return new DayResult(
                demand,
                soldByCompanies,
                soldByCompetitors,
                overflow,
                utilisation,
                priceBefore,
                price,
                competitorsBefore,
                competitors);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double starsFromReputation(double reputation) {
        return 1.0 + 4.0 * reputation / 100.0;
    }
}
