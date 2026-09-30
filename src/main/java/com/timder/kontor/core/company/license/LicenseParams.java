package com.timder.kontor.core.company.license;

/**
 * Settings for all licenses
 * @param referenceFeeRate The reference fee of a market is this rate times its base demand times its reference cost
 * @param applicationFeeMultiplier The application fee is this many times the rounded reference fee
 */
public record LicenseParams(
        double referenceFeeRate,
        int applicationFeeMultiplier
) {

    public LicenseParams {
        if (!(referenceFeeRate > 0.0) || Double.isInfinite(referenceFeeRate)) {
            throw new IllegalArgumentException("referenceFeeRate must be positive and finite.");
        }
        if (applicationFeeMultiplier < 1) throw new IllegalArgumentException("applicationFeeMultiplier must be at least 1.");
    }

    /*
    The following code is AI generated
     */

    public static LicenseParams standard() {
        return new LicenseParams(0.009, 3);
    }
}
