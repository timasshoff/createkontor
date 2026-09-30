package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.value.ItemId;

import java.util.*;

public final class LicenseHoldingRules {

    public enum CancelResult {
        /** The license is cancelled and will be billed one last time at the next day change. */
        CANCELLED,
        /** The license had been cancelled before. Nothing changed. */
        ALREADY_CANCELLED,
        /** The company does not own the license. Nothing changed. */
        NOT_HELD
    }

    /**
     * Checks what {@link  #acquire(Company, LicenseKey, Map, long, CompanyParams)} would do right now.
     * Does not change anything.
     * @param company The company
     * @param key The license key
     * @param referenceFees The current reference fee of every market
     * @param params The company parameters
     * @return What would happen
     */
    public static AcquireResult preview(Company company, LicenseKey key, Map<ItemId, Money> referenceFees, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(key, "key must not be null.");
        Objects.requireNonNull(referenceFees, "referenceFees must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        LicenseCatalog catalog = params.licenses();
        LicenseDef license = catalog.find(key).orElse(null);
        if (license == null) {
            return new AcquireResult(AcquireResult.Status.UNKNOWN_LICENSE, Money.ZERO);
        }

        LicenseHolding existing = company.license(key).orElse(null);
        if (existing != null && existing.isActive()) {
            return new AcquireResult(AcquireResult.Status.ALREADY_HELD, Money.ZERO);
        }
        if (company.legalLevel() < license.minLegalLevel()) {
            return new AcquireResult(AcquireResult.Status.LEGAL_LEVEL_TOO_LOW, Money.ZERO);
        }
        if (activeCount(company) >= company.legalForm(params).maxProductLicenses()) {
            return new AcquireResult(AcquireResult.Status.LIMIT_REACHED, Money.ZERO);
        }
        if (existing != null) {
            return new AcquireResult(AcquireResult.Status.REACTIVATED, Money.ZERO);
        }

        Money fee = LicenseRules.applicationFee(license, catalog.params(), referenceFees);
        if (!company.isOperational()) {
            return new AcquireResult(AcquireResult.Status.NOT_OPERATIONAL, fee);
        }
        if (fee.isPositive() && !company.canSpend(fee, params)) {
            return new AcquireResult(AcquireResult.Status.CANNOT_AFFORD, fee);
        }
        return new AcquireResult(AcquireResult.Status.ACQUIRED, fee);
    }

    public static AcquireResult acquire(Company company, LicenseKey key, Map<ItemId, Money> referenceFees, long day, CompanyParams params) {
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        AcquireResult result = preview(company, key, referenceFees, params);
        if (result.status() == AcquireResult.Status.REACTIVATED) {
            company.replaceLicense(company.license(key).orElseThrow().reactivate());
        } else if (result.status() == AcquireResult.Status.ACQUIRED) {
            if (result.fee().isPositive() && !company.trySpend(day, BookingKind.APPLICATION_FEE, result.fee(), key.toString(), params)) {
                throw new IllegalStateException("The company could pay " + result.fee() + " a moment ago but cannot now.");
            }
            Money dailyFee = LicenseRules.dailyFee(params.licenses().get(key), referenceFees);
            company.addLicense(LicenseHolding.acquire(key, day, dailyFee));
        }
        return result;
    }

    /**
     * Cancels a license. New requests stop at once.
     *
     * @param company The company. Is mutated on success.
     * @param key The license to cancel
     * @return What happened
     */
    public static CancelResult cancel(Company company, LicenseKey key) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(key, "key must not be null.");

        LicenseHolding holding = company.license(key).orElse(null);
        if (holding == null) {
            return CancelResult.NOT_HELD;
        }
        if (holding.cancelled()) {
            return CancelResult.ALREADY_CANCELLED;
        }
        company.replaceLicense(holding.cancel());
        return CancelResult.CANCELLED;
    }

    /**
     * Removes every cancelled license.
     *
     * @param company The company. Is mutated.
     * @return The removed licenses, in the order the company bought them
     */
    public static List<LicenseKey> dropCancelled(Company company) {
        Objects.requireNonNull(company, "company must not be null.");

        List<LicenseKey> dropped = new ArrayList<>();
        for (LicenseHolding holding : company.licenses()) {
            if (holding.cancelled()) {
                company.removeLicense(holding.key());
                dropped.add(holding.key());
            }
        }
        return dropped;
    }

    /**
     * @param company The company
     * @return The number of licenses that count towards the limit of the legal form: every one that is not cancelled.
     */
    public static int activeCount(Company company) {
        Objects.requireNonNull(company, "company must not be null.");
        int count = 0;
        for (LicenseHolding holding : company.licenses()) {
            if (holding.isActive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @return The licenses the company may use, in the order it bought them. Licenses missing in the catalog are left out.
     */
    public static List<LicenseDef> activeLicenses(Company company, LicenseCatalog catalog) {
        return definitions(company, catalog, false);
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @return The licenses that have to be billed at the next day change
     */
    public static List<LicenseDef> billableLicenses(Company company, LicenseCatalog catalog) {
        return definitions(company, catalog, true);
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @return The holdings whose license no longer exists in the catalog
     */
    public static List<LicenseHolding> unknownHoldings(Company company, LicenseCatalog catalog) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(catalog, "catalog must not be null.");

        List<LicenseHolding> unknown = new ArrayList<>();
        for (LicenseHolding holding : company.licenses()) {
            if (catalog.find(holding.key()).isEmpty()) {
                unknown.add(holding);
            }
        }
        return unknown;
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @return Every license the company owns, active, cancelled and unknown ones
     */
    public static List<HeldLicense> holdings(Company company, LicenseCatalog catalog) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(catalog, "catalog must not be null.");

        List<HeldLicense> result = new ArrayList<>();
        for (LicenseHolding holding : company.licenses()) {
            result.add(new HeldLicense(holding, catalog.find(holding.key())));
        }
        return result;
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @return The sum of the daily fees that are booked at the next day change
     */
    public static Money totalDailyFee(Company company, LicenseCatalog catalog) {
        Money total = Money.ZERO;
        for (HeldLicense held : holdings(company, catalog)) {
            if (held.billed()) {
                total = total.plus(held.dailyFee());
            }
        }
        return total;
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @param market A market
     * @return True if an active license of the company covers the market, so requests for it may be generated
     */
    public static boolean mayTrade(Company company, LicenseCatalog catalog, ItemId market) {
        Objects.requireNonNull(market, "market must not be null.");
        return LicenseRules.cheapestApplicable(activeLicenses(company, catalog), market).isPresent();
    }

    /**
     * @param company The company
     * @param catalog The license catalog
     * @return Every market covered by at least one active license of the company
     */
    public static List<ItemId> licensedMarkets(Company company, LicenseCatalog catalog) {
        Set<ItemId> markets = new LinkedHashSet<>();
        for (LicenseDef license : activeLicenses(company, catalog)) {
            markets.addAll(license.markets());
        }
        return List.copyOf(markets);
    }

    private static List<LicenseDef> definitions(Company company, LicenseCatalog catalog, boolean includeCancelled) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(catalog, "catalog must not be null.");

        List<LicenseDef> result = new ArrayList<>();
        for (LicenseHolding holding : company.licenses()) {
            if (holding.cancelled() && !includeCancelled) {
                continue;
            }
            catalog.find(holding.key()).ifPresent(result::add);
        }
        return result;
    }
}
