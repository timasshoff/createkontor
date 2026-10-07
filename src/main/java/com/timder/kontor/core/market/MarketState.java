package com.timder.kontor.core.market;

/**
 * The changeable state of a market.
 */
public final class MarketState {

    /**
     * Structural price level. Moves daily.
     */
    private double priceLevel;

    /**
     * Amount of competitors.
     */
    private double competitors;

    /**
     * Reputation of the competitors. Normally neutral, but events might change it.
     */
    private double competitorReputation;

    /**
     * The trading deviation caused by real-time purchases / deliveries.
     * Fast overlay on top of the daily updated price level.
     * Zero means no deviation at this moment.
     */
    private double deviation;

    /**
     * How much product competitors delivered today. Is cleared when a new day starts.
     */
    private double deliveredToday;

    /**
     * How much product competitors delivered this trading tick.
     * A trading tick is NOT a normal minecraft tick.
     * See {@link MarketRules}.
     */
    private double deliveredThisTick;

    /**
     * How much product company bought today
     */
    private double purchasedToday;

    /**
     * How much product companies bought from the competitors this trading tick
     */
    private double purchasedThisTick;

    /**
     * Creates a new market state representing the changeable state of a market.
     * @param priceLevel The structural price level
     * @param competitors Amount of competitors
     */
    public MarketState(double priceLevel, double competitors) {
        this.priceLevel = priceLevel;
        this.competitors = competitors;
        this.competitorReputation = MarketParams.NEUTRAL_REPUTATION;
        this.deviation = 0.0;
        this.deliveredToday = 0.0;
        this.deliveredThisTick = 0.0;
        this.purchasedToday = 0.0;
        this.purchasedThisTick = 0.0;
    }

    /**
     * Creates a fresh market with the equilibrium price and 3 competitors.
     * @param params The parameters of the market
     * @return The fresh market
     */
    public static MarketState fresh(MarketParams params) {
        return new MarketState(params.equilibriumPrice(), 3.0);
    }

    public double getPriceLevel() {
        return priceLevel;
    }

    public double getCompetitors() {
        return competitors;
    }

    public double getCompetitorReputation() {
        return competitorReputation;
    }

    public double getDeviation() {
        return deviation;
    }

    public double getDeliveredToday() {
        return deliveredToday;
    }

    public double getDeliveredThisTick() {
        return deliveredThisTick;
    }

    public double getPurchasedToday() {
        return purchasedToday;
    }

    public double getPurchasedThisTick() {
        return purchasedThisTick;
    }

    /**
     * Price, that clients see.
     * @return Displayed price
     */
    public double getDisplayedPrice() {
        return priceLevel * (1.0 + deviation);
    }

    /**
     * The capacity of all the competitors.
     * @param params The parameters of this market
     * @return The total capacity of all competitors
     */
    public double getCapacity(MarketParams params) {
        return competitors * params.plantSize();
    }

    /**
     * Visible amount of competitors.
     * @return The rounded number of competitors
     */
    public int getVisibleCompanies() {
        return (int) Math.round(competitors);
    }

    /**
     * Records a full-filled delivery.
     * @param quantity The amount of delivered product
     */
    public void recordDelivery(double quantity) {
        if (quantity > 0) {
            deliveredToday += quantity;
            deliveredThisTick += quantity;
        }
    }

    /**
     * Records a purchase of product from the competitors.
     * @param quantity The amount of bought product
     */
    public void recordPurchase(double quantity) {
        if (quantity > 0) {
            purchasedToday += quantity;
            purchasedThisTick += quantity;
        }
    }

    /**
     * Changes the reputation of the competitors. Used by events, clamped to one to five stars.
     * @param value The new reputation in stars
     */
    public void setCompetitorReputation(double value) {
        this.competitorReputation = MarketRules.clamp(value, 1.0, 5.0);
    }

    void setPriceLevel(double value) {
        this.priceLevel = value;
    }

    void setCompetitors(double value) {
        this.competitors = value;
    }

    void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    void clearDeliveredToday() {
        this.deliveredToday = 0.0;
    }

    void clearDeliveredThisTick() {
        this.deliveredThisTick = 0.0;
    }

    void clearPurchasedToday() {
        this.purchasedToday = 0.0;
    }

    void clearPurchasedThisTick() {
        this.purchasedThisTick = 0.0;
    }

    @Override
    public String toString() {
        return "MarketState[price=%.2f, competitors=%.2f, deliveredToday=%.0f]".formatted(priceLevel, competitors, deliveredToday);
    }

    public record SaveState(double priceLevel, double competitors, double competitorReputation, double deviation, double deliveredToday, double deliveredThisTick, double purchasedToday, double purchasedThisTick) {
        public SaveState(double priceLevel, double competitors, double competitorReputation, double deviation, double deliveredToday, double deliveredThisTick) {
            this(priceLevel, competitors, competitorReputation, deviation, deliveredToday, deliveredThisTick, 0.0, 0.0);
        }
    }

    public SaveState getSaveState() {
        return new SaveState(priceLevel, competitors, competitorReputation, deviation, deliveredToday, deliveredThisTick, purchasedToday, purchasedThisTick);
    }

    public static MarketState restore(SaveState snapshot) {
        MarketState state = new MarketState(snapshot.priceLevel(), snapshot.competitors());
        state.competitorReputation = snapshot.competitorReputation();
        state.deviation = snapshot.deviation();
        state.deliveredToday = snapshot.deliveredToday();
        state.deliveredThisTick = snapshot.deliveredThisTick();
        state.purchasedToday = snapshot.purchasedToday();
        state.purchasedThisTick = snapshot.purchasedThisTick();
        return state;
    }
}
