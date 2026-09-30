package com.timder.kontor.core.market;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.license.*;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.value.ItemId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

public final class MarketParticipationRules {

    public static final double START_REPUTATION = 50.0;

    public record CancelOutcome(LicenseHoldingRules.CancelResult result, List<ItemId> withdrawnMarkets) {

        public CancelOutcome {
            Objects.requireNonNull(result, "result must not be null.");
            withdrawnMarkets = List.copyOf(withdrawnMarkets);
            if (result != LicenseHoldingRules.CancelResult.CANCELLED && !withdrawnMarkets.isEmpty()) {
                throw new IllegalArgumentException("Only a cancelled license can make the company leave markets.");
            }
        }
    }

    public record LicensedMarket(ItemId market, LicenseDef license, boolean participating, OptionalDouble storedReputation) {

        public LicensedMarket {
            Objects.requireNonNull(market, "market must not be null.");
            Objects.requireNonNull(license, "license must not be null.");
            Objects.requireNonNull(storedReputation, "storedReputation must not be null.");
            if (!license.coversMarket(market)) throw new IllegalArgumentException("The license does not cover the market.");
            if (participating && storedReputation.isEmpty()) throw new IllegalArgumentException("A participant has a stored reputation.");
        }

        /**
         * @return True, if the company took part before and is paused now
         */
        public boolean paused() {
            return !participating && storedReputation.isPresent();
        }
    }

    /**
     * Lets a company take part in a market. Either by resuming a paused participation or by registering a new participant.
     * @param company The company
     * @param economy The economy
     * @param market The market to join
     * @param params The company parameters
     * @return
     */
    public static MarketParticipant join(Company company, Economy economy, ItemId market, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(market, "market must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        requireMarket(economy, market);
        if (economy.isParticipant(market, company.id())) {
            throw new IllegalStateException(company.id() + " already takes part in the market of " + market + ".");
        }
        if (!LicenseHoldingRules.mayTrade(company, params.licenses(), market)) {
            throw new IllegalStateException(company.id() + " has no active license for the market of " + market + ".");
        }
        if (!company.isOperational()) {
            throw new IllegalStateException(company.id() + " is in payment difficulties and cannot take part in a new market.");
        }

        if (economy.isPaused(market, company.id())) {
            economy.resumeParticipant(market, company.id());
        } else {
            double listPrice = economy.marketSnapshot(market).displayedPrice();
            economy.registerParticipant(market, company.id(), listPrice, START_REPUTATION);
        }
        return economy.storedParticipant(market, company.id()).orElseThrow();
    }

    /**
     * Pauses the company in a market. Open requests are removed.
     * @param company The company
     * @param economy The economy
     * @param market The market
     * @return The amount of removed open requests
     */
    public static int pause(Company company, Economy economy, ItemId market) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(market, "market must not be null.");

        requireMarket(economy, market);
        if (!economy.isParticipant(market, company.id())) {
            throw new IllegalStateException(company.id() + " does not actively take part in the market of " + market + ".");
        }

        economy.pauseParticipant(market, company.id());
        return company.requestBoard().removeOpenRequests(market).size();
    }

    /**
     * Withdraws a company from a market. Open requests and the reputation of the company for this market is removed.
     * @param company The company
     * @param economy The economy
     * @param market The market
     * @return The amount of removed open requests
     */
    public static int withdraw(Company company, Economy economy, ItemId market) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(market, "market must not be null.");

        requireMarket(economy, market);
        if (economy.storedParticipant(market, company.id()).isEmpty()) {
            throw new IllegalStateException(company.id() + " is neither active nor paused in the market of " + market + ".");
        }

        economy.withdrawParticipant(market, company.id());
        return company.requestBoard().removeOpenRequests(market).size();
    }

    /**
     * Withdraws the company from every market it is active or paused in but has no active license covering it
     * @param company The company
     * @param economy The economy
     * @param params The company parameters
     * @return The markets the company was withdrawn from
     */
    public static List<ItemId> syncParticipation(Company company, Economy economy, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        List<ItemId> withdrawn = new ArrayList<>();
        for (ItemId market : economy.marketIds()) {
            if (economy.storedParticipant(market, company.id()).isPresent() && !LicenseHoldingRules.mayTrade(company, params.licenses(), market)) {
                withdraw(company, economy, market);
                withdrawn.add(market);
            }
        }
        return withdrawn;
    }

    /**
     * Cancels a license and withdraws the company from the markets that no other active license covers
     *
     * @param company The company
     * @param economy The economy
     * @param key The license to cancel
     * @param params The company parameters
     * @return What happened to the license and which markets the company was withdrawn from
     */
    public static CancelOutcome cancelLicense(Company company, Economy economy, LicenseKey key, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(key, "key must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        LicenseHoldingRules.CancelResult result = LicenseHoldingRules.cancel(company, key);
        if (result != LicenseHoldingRules.CancelResult.CANCELLED) {
            return new CancelOutcome(result, List.of());
        }
        return new CancelOutcome(result, syncParticipation(company, economy, params));
    }

    /**
     * Every market a company can participate in.
     * @param company The company
     * @param economy The economy
     * @param params The company parameters
     * @return The licensed markets
     */
    public static List<LicensedMarket> licensedMarkets(Company company, Economy economy, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        LicenseCatalog catalog = params.licenses();
        List<LicenseDef> active = LicenseHoldingRules.activeLicenses(company, catalog);
        List<ItemId> existing = economy.marketIds();

        List<LicensedMarket> result = new ArrayList<>();
        for (ItemId market : LicenseHoldingRules.licensedMarkets(company, catalog)) {
            if (!existing.contains(market)) {
                continue;
            }
            LicenseDef applicable = LicenseRules.cheapestApplicable(active, market).orElseThrow();
            OptionalDouble reputation = economy.storedParticipant(market, company.id())
                    .map(participant -> OptionalDouble.of(participant.reputation()))
                    .orElse(OptionalDouble.empty());
            result.add(new LicensedMarket(market, applicable, economy.isParticipant(market, company.id()), reputation));
        }
        return result;
    }

    /**
     * @param company The company
     * @param economy The economy
     * @return Every market the company is paused in
     */
    public static List<ItemId> pausedMarkets(Company company, Economy economy) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");

        List<ItemId> result = new ArrayList<>();
        for (ItemId market : economy.marketIds()) {
            if (economy.isPaused(market, company.id())) {
                result.add(market);
            }
        }
        return result;
    }

    /**
     * @param company The company
     * @param economy The economy
     * @return Every market the company actively participates in
     */
    public static List<ItemId> participations(Company company, Economy economy) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");

        List<ItemId> result = new ArrayList<>();
        for (ItemId market : economy.marketIds()) {
            if (economy.isParticipant(market, company.id())) {
                result.add(market);
            }
        }
        return result;
    }

    private static void requireMarket(Economy economy, ItemId market) {
        if (!economy.marketIds().contains(market)) {
            throw new IllegalArgumentException("There is no market for " + market + ".");
        }
    }
}
