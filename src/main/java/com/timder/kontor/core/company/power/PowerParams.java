package com.timder.kontor.core.company.power;

public record PowerParams(double pricePerSuDay, double dynamicPriceFactor) {

    public PowerParams {
        if (!(pricePerSuDay >= 0.0) || Double.isInfinite(pricePerSuDay))
            throw new IllegalArgumentException("pricePerSuDay must be a finite number, not negative.");
        if (!(dynamicPriceFactor >= 1.0) || Double.isInfinite(dynamicPriceFactor))
            throw new IllegalArgumentException("dynamicPriceFactor must be a finite number, at least 1.");
    }

    public double factorOf(PowerKind kind) {
        return switch (kind) {
            case FIXED -> 1.0;
            case DYNAMIC -> dynamicPriceFactor;
        };
    }

    /*
    The following code is AI generated
     */

    public static PowerParams standard() {
        return new PowerParams(0.015, 1.5);
    }
}
