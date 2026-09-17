package com.timder.kontor.core.raw;

/**
 * The changeable state of one raw material.
 */
public final class RawMaterialState {

    private double price;
    private double purchasedToday;

    public RawMaterialState(double price) {
        this.price = price;
        this.purchasedToday = 0.0;
    }

    /**
     * Creates a fresh material state priced at its base value.
     * @param params The params of the material
     * @return The created state
     */
    public static RawMaterialState fresh(RawMaterialParams params) {
        return new RawMaterialState(params.baseValue());
    }

    public double getPrice() {
        return price;
    }

    public double getPurchasedToday() {
        return purchasedToday;
    }

    /**
     * Records a purchase.
     * @param quantity The amount of bought material
     */
    public void recordPurchase(double quantity) {
        if (quantity > 0) {
            purchasedToday += quantity;
        }
    }

    void setPrice(double value) {
        this.price = value;
    }

    void clearPurchasedToday() {
        this.purchasedToday = 0.0;
    }

    @Override
    public String toString() {
        return "RawMaterialState[price=%.4f, purchasedToday=%.1f]".formatted(price, purchasedToday);
    }

    public record SaveState(double price, double purchasedToday) {}

    public SaveState getSaveState() {
        return new SaveState(price, purchasedToday);
    }

    public static RawMaterialState restore(SaveState snapshot) {
        RawMaterialState state = new RawMaterialState(snapshot.price());
        state.purchasedToday = snapshot.purchasedToday();
        return state;
    }
}
