package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.value.ItemId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class LicenseRules {

    /**
     * Calculates the reference fee of one market
     * @param params The params
     * @param baseDemand The base demand
     * @param referenceCost The current reference cost
     * @return The reference fee per day
     */
    public static Money referenceFee(LicenseParams params, double baseDemand, double referenceCost) {
        Objects.requireNonNull(params, "params must not be null.");
        requirePositiveFinite(baseDemand, "baseDemand");
        requirePositiveFinite(referenceCost, "referenceCost");

        BigDecimal dollars = BigDecimal.valueOf(params.referenceFeeRate())
                .multiply(BigDecimal.valueOf(baseDemand))
                .multiply(BigDecimal.valueOf(referenceCost));
        return roundToMoney(dollars.movePointRight(2));
    }

    /**
     * Sums all reference fees of a license
     * @param license The license
     * @param referenceFees The reference fees
     * @return The summed reference fees for all markets of the license
     */
    public static Money totalReferenceFee(LicenseDef license, Map<ItemId, Money> referenceFees) {
        Objects.requireNonNull(license, "license must not be null.");
        Objects.requireNonNull(referenceFees, "referenceFees must not be null.");

        Money sum = Money.ZERO;
        for (ItemId market : license.markets()) {
            Money fee = referenceFees.get(market);
            if (fee == null) {
                throw new IllegalArgumentException("No reference fee for market " + market + ".");
            }
            if (fee.isNegative()) {
                throw new IllegalArgumentException("The reference fee of market " + market + " must not be negative.");
            }
            sum = sum.plus(fee);
        }
        return sum;
    }

    /**
     * The daily fee a company has to pay each day for holding the license
     * @param license The license
     * @param referenceFees The current reference fee of each market
     * @return The daily fee, rounded once
     */
    public static Money dailyFee(LicenseDef license, Map<ItemId, Money> referenceFees) {
        Money total = totalReferenceFee(license, referenceFees);
        return scale(total, license.feeFactor(), license.dailyFraction());
    }

    /**
     * Calculates the one-time application fee to obtain the license.
     * @param license The license
     * @param params The license parameters
     * @param referenceFees The current reference fee of each market
     * @return The application fee, rounded once
     */
    public static Money applicationFee(LicenseDef license, LicenseParams params, Map<ItemId, Money> referenceFees) {
        Objects.requireNonNull(params, "params must not be null.");
        Money total = totalReferenceFee(license, referenceFees).multipliedBy(params.applicationFeeMultiplier());
        return scale(total, license.feeFactor());
    }

    /**
     * The share of an orders revenue that a company owes to the license
     * @param license The license
     * @param revenue The revenue
     * @return The revenue share
     */
    public static Money revenueShare(LicenseDef license, Money revenue) {
        Objects.requireNonNull(license, "license must not be null.");
        Objects.requireNonNull(revenue, "revenue must not be null.");
        if (revenue.isNegative()) {
            throw new IllegalArgumentException("revenue must not be negative.");
        }
        return scale(revenue, license.revenueShare());
    }

    /**
     * Chooses the cheapest license for a market.
     * @param held The licenses to choose from
     * @param market The market
     * @return The cheapest license
     */
    public static Optional<LicenseDef> cheapestApplicable(Collection<LicenseDef> held, ItemId market) {
        Objects.requireNonNull(held, "held must not be null.");
        Objects.requireNonNull(market, "market must not be null.");

        LicenseDef best = null;
        for (LicenseDef license : held) {
            if (!license.coversMarket(market)) {
                continue;
            }
            if (best == null || license.revenueShare() < best.revenueShare()) {
                best = license;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * The markets of a license that already owned licenses already cover
     * @param candidate The candidate license
     * @param held The already owned licenses
     * @return The markets that are already covered by the owned licenses
     */
    public static Set<ItemId> alreadyCovered(LicenseDef candidate, Collection<LicenseDef> held) {
        Objects.requireNonNull(candidate, "candidate must not be null.");
        Objects.requireNonNull(held, "held must not be null.");

        Set<ItemId> covered = new LinkedHashSet<>();
        for (ItemId market : candidate.markets()) {
            for (LicenseDef license : held) {
                if (license.coversMarket(market)) {
                    covered.add(market);
                    break;
                }
            }
        }
        return Collections.unmodifiableSet(covered);
    }

    private static Money scale(Money amount, double... factors) {
        BigDecimal exact = BigDecimal.valueOf(amount.cents());
        for (double factor : factors) {
            exact = exact.multiply(BigDecimal.valueOf(factor));
        }
        return roundToMoney(exact);
    }

    private static void requirePositiveFinite(double value, String name) {
        if (!(value > 0.0) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be positive and finite.");
        }
    }

    private static Money roundToMoney(BigDecimal cents) {
        return Money.ofCents(cents.setScale(0, RoundingMode.HALF_UP).longValueExact());
    }
}
