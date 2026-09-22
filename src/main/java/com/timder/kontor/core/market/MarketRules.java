package com.timder.kontor.core.market;

public final class MarketRules {

    public static final double MAX_PRICE_FACTOR = 2.0;
    public static final double MIN_COMPANIES = 1.0;
    public static final double MAX_COMPANIES = 8.0;

    public static final int TRADING_TICKS_PER_DAY = 12;

    /**
     * How much deviation survives from one trading tick to another
     */
    public static final double DEVIATION_DECAY = 0.94;

    /**
     * How strongly a surprise moves the deviation
     */
    public static final double DEVIATION_STRENGTH = 0.02;

    /**
     * Max movement a deviation can cause
     */
    public static final double DEVIATION_BOUND = 0.5; // Max movement that deviation can cause

    /**
     * How appealing a supplier is to customers.
     * Built from reputation and price. Both factors are relative to a neutral point.
     * A supplier with a neutral reputation asking the exact market price scores 1.0.
     * A higher price sensitivity means customers chase the cheapest offer, a high reputation weight means
     * customers pay for a good name.
     *
     * @param price The price per unit the supplier asks for
     * @param reputation The reputation of the supplier
     * @param state The current state of the market
     * @param params The parameters of the market
     * @return The attractiveness of that supplier. 1.0 means neutral.
     */
    public static double attractiveness(double price, double reputation, MarketState state, MarketParams params) {
        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive.");
        }

        double reputationFactor = Math.pow(reputation / MarketParams.NEUTRAL_REPUTATION, params.reputationWeight());
        double priceFactor = Math.pow(price / state.getDisplayedPrice(), -params.priceSensitivity());

        return  reputationFactor * priceFactor;
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
        double reputationFactor = Math.pow(state.getCompetitorReputation() / MarketParams.NEUTRAL_REPUTATION, params.reputationWeight());
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
    public static double shareFrom(double ownAttractiveness, double otherCompaniesAttractiveness, MarketState state, MarketParams params) {
        double total = ownAttractiveness + otherCompaniesAttractiveness + competitorAttractiveness(state, params);
        return total > 0 ? ownAttractiveness / total : 0.0;
    }

    public static double share(double price, double reputation, MarketState state, MarketParams params) {
        return shareFrom(attractiveness(price, reputation, state, params), 0.0, state, params);
    }

    /**
     * Advanced the trading tick forward. Moves the trading deviation.
     * @param state The market state
     * @param actualDelivered The amount of actually delivered product across the market
     * @param expectedDelivered The expected amount of delivered product by all competitors and concurrence, based on their market shared
     * @param demandThisTick The demand for this tick (usually daily demand divided by amount of trading ticks per day)
     */
    public static void advanceTradingTick(MarketState state, double actualDelivered, double expectedDelivered, double demandThisTick) {
        double surprise = actualDelivered - expectedDelivered;
        double reference = demandThisTick > 0 ? demandThisTick : 1.0;

        double deviation = DEVIATION_DECAY * state.getDeviation() - DEVIATION_STRENGTH * surprise / reference;

        state.setDeviation(clamp(deviation, -DEVIATION_BOUND, DEVIATION_BOUND));
        state.clearDeliveredThisTick();
    }

    public static DayResult advanceDay(MarketState state, MarketParams params, double demand) {
        double priceBefore = state.getPriceLevel();
        double competitorsBefore = state.getCompetitors();

        // The capacity of the competitors
        double capacity = state.getCapacity(params);

        // The remaining demand
        double soldByCompanies = Math.min(state.getDeliveredToday(), demand);
        double remaining = Math.max(0.0, demand - soldByCompanies);

        // The performance of competitors
        double soldByCompetitors = Math.min(remaining, capacity);
        double overflow = remaining - soldByCompetitors;

        // Utilisation
        double utilisation = capacity > 0 ? soldByCompetitors / capacity : 0.0;

        // Adapt price level
        double price = priceBefore * (1.0 + params.priceResponse() * (utilisation - params.targetUtilisation()));
        price = clamp(price, params.referenceCost(), params.referenceCost() * MAX_PRICE_FACTOR);
        state.setPriceLevel(price);

        // Profitability
        double margin = (price - params.referenceCost()) / params.referenceCost();
        double profitability = utilisation * margin;
        double target = params.targetProfitability();
        double relative = clamp((profitability - target) / target, -1.0, 1.0);

        // Adapt amount of competitors
        double competitors = clamp(competitorsBefore * (1.0 + params.capacityResponse() * relative), MIN_COMPANIES, MAX_COMPANIES);
        state.setCompetitors(competitors);

        // Clearing counter
        state.clearDeliveredToday();

        return new DayResult(demand, soldByCompanies, soldByCompetitors, overflow, utilisation, priceBefore, price, competitorsBefore, competitors);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double starsFromReputation(double reputation) {
        return 1.0 + 4.0 * reputation / 100.0;
    }
}
