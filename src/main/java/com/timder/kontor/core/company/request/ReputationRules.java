package com.timder.kontor.core.company.request;

import com.timder.kontor.core.company.LegalFormDef;
import com.timder.kontor.core.company.order.Order;

import java.util.Objects;

public final class ReputationRules {

    /**
     * Changes for a fully delivered on time order.
     * @param currentReputation The products current reputation
     * @param order The settled order
     * @param packageSize The products package size
     * @param structuralMarketPrice The markets structural price level
     * @param params The reputation parameters
     * @return The new reputation
     */
    public static double changeOnTime(double currentReputation, Order order, int packageSize, double structuralMarketPrice, ReputationParams params) {
        requireValidInputs(currentReputation, order, params);
        double w = weight(order, packageSize, structuralMarketPrice);
        double delta = params.onTimeGain() * w * (1 + params.onTimeUrgencyFactor() * order.getUrgency()) * Math.min(1.0, (100.0 - currentReputation) / 50.0);
        return clamp(currentReputation + delta);
    }

    /**
     * Changes for a fully delivered but late order.
     * @param currentReputation The products current reputation
     * @param order The settled order
     * @param packageSize The products package size
     * @param structuralMarketPrice The markets structural price level
     * @param params The reputation parameters
     * @return The new reputation
     */
    public static double changeLate(double currentReputation, Order order, int packageSize, double structuralMarketPrice, ReputationParams params) {
        requireValidInputs(currentReputation, order, params);
        double w = weight(order, packageSize, structuralMarketPrice);
        double delta = -params.lateLoss() * w * Math.min(1.0, currentReputation / 50.0);
        return clamp(currentReputation + delta);
    }

    /**
     * Changes for a failed order.
     * @param currentReputation The products current reputation
     * @param order The settled order
     * @param packageSize The products package size
     * @param structuralMarketPrice The markets structural price level
     * @param params The reputation parameters
     * @return The new reputation
     */
    public static double changeFailed(double currentReputation, Order order, LegalFormDef legalForm, int packageSize, double structuralMarketPrice, ReputationParams params) {
        requireValidInputs(currentReputation, order, params);
        Objects.requireNonNull(legalForm, "legalForm must not be null.");

        double w = weight(order, packageSize, structuralMarketPrice);
        double founderFactor = legalForm.founderProtection() ? params.founderProtectionFactor() : 1.0;
        double delta = -params.failureLoss() * w * order.getOrigin().reputationSurchargeFactor() * founderFactor * Math.min(1.0, currentReputation / 50.0);
        return clamp(currentReputation + delta);
    }

    private static double weight(Order order, int packageSize, double structuralMarketPrice) {
        if (packageSize <= 0) throw new IllegalArgumentException("packageSize must be positive.");
        if (structuralMarketPrice <= 0) throw new IllegalArgumentException("structuralMarketPrice must be positive.");

        double orderValue = order.getQuantity() * order.getUnitPrice();
        double referenceValue = 4.0 * packageSize * structuralMarketPrice;
        double raw = Math.sqrt(orderValue / referenceValue);
        return Math.max(0.5, Math.min(2.0, raw));
    }

    private static void requireValidInputs(double currentReputation, Order order, ReputationParams params) {
        Objects.requireNonNull(order, "order must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        if (currentReputation < 0 || currentReputation > 100) {
            throw new IllegalArgumentException("currentReputation must be between 0 and 100.");
        }
    }

    private static double clamp(double reputation) {
        return Math.max(0.0, Math.min(100.0, reputation));
    }
}
