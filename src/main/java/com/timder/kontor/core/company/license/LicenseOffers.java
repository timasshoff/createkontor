package com.timder.kontor.core.company.license;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LicenseOffers {

    /**
     * @param economy The Economy
     * @param params The license parameters
     * @return The current reference fee for every market
     */
    public static Map<ItemId, Money> referenceFees(Economy economy, LicenseParams params) {
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        return LicenseRules.referenceFees(params, economy.marketDefinitions(), market -> economy.marketSnapshot(market).referenceCost());
    }

    /**
     * Lists every license of the catalog as an offer
     * @param company The company
     * @param economy The economy
     * @param params The company parameters
     * @return All offers
     */
    public static List<LicenseOffer> list(Company company, Economy economy, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        LicenseCatalog catalog = params.licenses();
        Map<ItemId, Money> fees = referenceFees(economy, catalog.params());
        List<LicenseDef> active = LicenseHoldingRules.activeLicenses(company, catalog);

        List<LicenseOffer> offers = new ArrayList<>();
        for (LicenseDef license : catalog.all()) {
            offers.add(new LicenseOffer(
                    license,
                    LicenseRules.dailyFee(license, fees),
                    LicenseRules.applicationFee(license, catalog.params(), fees),
                    LicenseRules.alreadyCovered(license, active),
                    company.license(license.key()).map(LicenseHolding::dailyFee),
                    LicenseHoldingRules.preview(company, license.key(), fees, params).status()));
        }
        return offers;
    }

    /**
     * Buys a license at the current reference fees of the economy
     * @param company The company
     * @param economy The economy
     * @param key The license to buy
     * @param day The current day (for booking)
     * @param params The company parameters
     * @return What happened
     */
    public static AcquireResult acquire(Company company, Economy economy, LicenseKey key, long day, CompanyParams params) {
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        return LicenseHoldingRules.acquire(company, key, referenceFees(economy, params.licenses().params()), day, params);
    }
}
