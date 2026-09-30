package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.value.ItemId;

import java.util.*;

public final class LicenseCatalog {

    private final LicenseParams params;
    private final List<LicenseDef> licenses;
    private final Map<LicenseKey, LicenseDef> byKey = new LinkedHashMap<>();
    private final Map<ItemId, List<LicenseDef>> byMarket = new LinkedHashMap<>();

    private LicenseCatalog(LicenseParams params, List<LicenseDef> licenses) {
        this.params = params;
        this.licenses = List.copyOf(licenses);
        for (LicenseDef license : this.licenses) {
            if (byKey.put(license.key(), license) != null) {
                throw new IllegalArgumentException("duplicate license " + license.key() + ".");
            }
            for (ItemId market : license.markets()) {
                byMarket.computeIfAbsent(market, k -> new ArrayList<>()).add(license);
            }
        }
        byMarket.replaceAll((market, list) -> List.copyOf(list));
    }

    public static LicenseCatalog empty(LicenseParams params) {
        Objects.requireNonNull(params, "params must not be null.");
        return new LicenseCatalog(params, List.of());
    }

    public static LicenseCatalog build(LicenseParams params, List<LicenseDef> defined, List<ItemId> markets, LegalForms legalForms) {
        Objects.requireNonNull(params, "params must not be null.");
        Objects.requireNonNull(defined, "defined must not be null.");
        Objects.requireNonNull(markets, "markets must not be null.");
        Objects.requireNonNull(legalForms, "legalForms must not be null.");

        Set<LicenseKey> keys = new HashSet<>();
        for (LicenseDef license : defined) {
            if (license.isGenerated()) {
                throw new IllegalArgumentException("License " + license.key() + " is generated but was passed in as defined.");
            }
            if (!keys.add(license.key())) {
                throw new IllegalArgumentException("duplicate license id " + license.key() + ".");
            }
        }

        Set<ItemId> known = new LinkedHashSet<>(markets);

        List<LicenseDef> kept = new ArrayList<>();
        for (LicenseDef license : defined) {
            Set<ItemId> remaining = new LinkedHashSet<>();
            for (ItemId market : license.markets()) {
                if (known.contains(market)) {
                    remaining.add(market);
                }
            }
            if (remaining.isEmpty()) {
                continue;
            }

            kept.add(remaining.size() == license.markets().size()
                    ? license
                    : new LicenseDef(license.key(), remaining, license.feeFactor(), license.dailyFraction(), license.revenueShare(), license.minLegalLevel()));
        }

        Set<ItemId> covered = new LinkedHashSet<>();
        for (LicenseDef license : kept) {
            covered.addAll(license.markets());
        }
        List<LicenseDef> all = new ArrayList<>(kept);
        for (ItemId market : known) {
            if (covered.contains(market)) {
                continue;
            }
            all.add(generate(market));
        }

        return new LicenseCatalog(params, all);
    }

    private static LicenseDef generate(ItemId market) {
        return new LicenseDef(new LicenseKey.Generated(market), Set.of(market), 1.0, 1.0, 0.0, 1);
    }

    public LicenseParams params() {
        return params;
    }

    public List<LicenseDef> all() {
        return licenses;
    }

    public int size() {
        return licenses.size();
    }

    public Optional<LicenseDef> find(LicenseKey key) {
        return Optional.ofNullable(byKey.get(key));
    }

    public LicenseDef get(LicenseKey key) {
        LicenseDef license = byKey.get(key);
        if (license == null) {
            throw new IllegalArgumentException("no license " + key + ".");
        }
        return license;
    }

    public List<LicenseDef> licensesCovering(ItemId market) {
        return byMarket.getOrDefault(market, List.of());
    }

    public Set<ItemId> coveredMarkets() {
        return Collections.unmodifiableSet(byMarket.keySet());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LicenseCatalog catalog && params.equals(catalog.params) && licenses.equals(catalog.licenses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params, licenses);
    }

    @Override
    public String toString() {
        return "LicenseCatalog[" + licenses.size() + " licenses]";
    }
}
