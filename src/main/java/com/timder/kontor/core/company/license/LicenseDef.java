package com.timder.kontor.core.company.license;

import com.timder.kontor.core.value.ItemId;

import java.util.*;

/**
 * The definition of a license
 *
 * @param key The identity
 * @param markets The markets that this license opens
 * @param feeFactor Factor on the reference fee, positive TODO explain
 * @param dailyFraction The part of the (factored) reference fee that is due every day, 0 to 1
 * @param revenueShare The share of the revenue that is due, at least 0 and below 1
 * @param minLegalLevel The lowest legal form level a company needs to acquire the license, at least 1
 */
public record LicenseDef(
        LicenseKey key,
        Set<ItemId> markets,
        double feeFactor,
        double dailyFraction,
        double revenueShare,
        int minLegalLevel
) {

    public LicenseDef {
        Objects.requireNonNull(key, "key must not be null.");
        Objects.requireNonNull(markets, "markets must not be null.");
        markets = Collections.unmodifiableSet(new LinkedHashSet<>(markets));
        if (markets.isEmpty()) throw new IllegalArgumentException("markets must not be empty.");
        if (markets.contains(null)) throw new IllegalArgumentException("markets must not contain null.");
        if (!(feeFactor > 0.0) || Double.isInfinite(feeFactor)) throw new IllegalArgumentException("feeFactor must be positive and finite.");
        if (!(dailyFraction >= 0.0 && dailyFraction <= 1.0)) throw new IllegalArgumentException("dailyFraction must be between 0 and 1.");
        if (!(revenueShare >= 0.0 && revenueShare < 1.0)) throw new IllegalArgumentException("revenueShare must be at least 0 and below 1.");
        if (minLegalLevel < 1) throw new IllegalArgumentException("minLegalLevel must be at least 1.");
        if (key instanceof LicenseKey.Generated generated && !markets.equals(Set.of(generated.market()))) {
            throw new IllegalArgumentException("A generated license must cover exactly the market it was generated for.");
        }
    }

    public static LicenseDef defined(String id, Set<ItemId> markets, double feeFactor, double dailyFraction, double revenueShare, int minLegalLevel) {
        return new LicenseDef(new LicenseKey.Defined(id), markets, feeFactor, dailyFraction, revenueShare, minLegalLevel);
    }

    public boolean isGenerated() {
        return key instanceof LicenseKey.Generated;
    }

    public Optional<String> translationKey() {
        return key instanceof LicenseKey.Defined defined ? Optional.of(defined.translationKey()) : Optional.empty();
    }

    public boolean isBundle() {
        return markets.size() > 1;
    }

    public boolean coversMarket(ItemId market) {
        return markets.contains(market);
    }

    public boolean hasRevenueShare() {
        return revenueShare > 0.0;
    }
}
