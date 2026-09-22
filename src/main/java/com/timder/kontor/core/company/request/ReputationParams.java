package com.timder.kontor.core.company.request;

/**
 * Immutable global parameters for the reputation system
 * @param onTimeGain The reputation gained when a request is full-filled on time
 * @param onTimeUrgencyFactor The bonus for urgent requests
 * @param lateLoss The loss of reputation when a company is late
 * @param failureLoss The loss of reputation when a company fails to deliver
 * @param founderProtectionFactor Protection for legal form level 1
 * @param driftPerDay Daily movement of the reputation back to 50 for products without full-filled requests
 */
public record ReputationParams(
        double onTimeGain,
        double onTimeUrgencyFactor,
        double lateLoss,
        double failureLoss,
        double founderProtectionFactor,
        double driftPerDay
) {

    public ReputationParams {
        if (onTimeGain < 0) throw new IllegalArgumentException("onTimeGain must not be negative.");
        if (onTimeUrgencyFactor < 0) throw new IllegalArgumentException("onTimeUrgencyFactor must not be negative.");
        if (lateLoss < 0) throw new IllegalArgumentException("lateLoss must not be negative.");
        if (failureLoss < 0) throw new IllegalArgumentException("failureLoss must not be negative.");
        if (founderProtectionFactor < 0 || founderProtectionFactor > 1) throw new IllegalArgumentException("founderProtectionFactor must be between 0 and 1.");
        if (driftPerDay < 0) throw new IllegalArgumentException("driftPerDay must not be negative.");
    }

    /*
    The following code is AI generated
     */

    public static ReputationParams standard() {
        return new ReputationParams(
                1.5,    // on-time gain
                0.5,    // on-time urgency factor
                1.0,    // late loss
                4.0,    // failure loss
                0.5,    // founder protection factor
                0.5     // drift per day toward 50
        );
    }
}
