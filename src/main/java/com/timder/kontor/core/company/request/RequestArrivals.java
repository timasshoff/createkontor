package com.timder.kontor.core.company.request;

import com.timder.kontor.core.company.*;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.core.market.MarketParticipant;
import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.value.ItemId;

import java.util.*;

public final class RequestArrivals {

    private static final double SALES_REP_FACTOR = 1.0; // TODO Change once sales representatives get added
    private static final double FRAMEWORK_CONTRACT_QUANTITY = 0.0;

    public record Arrival(CompanyId companyId, ItemId product, Request request) {
        public boolean lost() {
            return request == null;
        }
    }

    private record Key(CompanyId company, ItemId market) { }

    /**
     * The countdown of one company for one product
     */
    private static final class Slot {
        private final double rate;
        private final double listPrice;
        private final double reputation;
        private final int legalLevel;
        private long countdown;

        private Slot(double rate, double listPrice, double reputation, int legalLevel) {
            this.rate = rate;
            this.listPrice = listPrice;
            this.reputation = reputation;
            this.legalLevel = legalLevel;
        }

        /**
         * A slot that never lets a request arrive, because the company gets no requests at all right now.
         */
        private boolean never() {
            return rate <= 0;
        }

        private boolean isOutdated(MarketParticipant participant, int currentLegalLevel) {
            return listPrice != participant.listPrice()
                    || reputation != participant.reputation()
                    || legalLevel != currentLegalLevel;
        }
    }

    private final Map<Key, Slot> slots = new HashMap<>();
    private final Rng rng;

    public RequestArrivals(Rng rng) {
        this.rng = Objects.requireNonNull(rng, "rng must not be null.");
    }

    /**
     * Runs the countdowns of all companies down.
     * Lets the requests arrive whenever the countdown for a company reaches zero.
     * @param registry The companies
     * @param economy The economy
     * @param companyParams The company parameters
     * @param requestParams The request parameters
     * @param ticks How many ticks passed since the last call
     * @param tradingTickPassed True, if a trading tick passed since the last call
     * @return Everything that arrived
     */
    public List<Arrival> advance(CompanyRegistry registry,
                                 Economy economy,
                                 CompanyParams companyParams,
                                 RequestParams requestParams,
                                 long ticks,
                                 boolean tradingTickPassed) {
        if (ticks <= 0) throw new IllegalArgumentException("ticks must be positive");

        if (tradingTickPassed) {
            slots.clear();
        }

        List<Arrival> arrivals = new ArrayList<>();
        Set<Key> active = new HashSet<>();
        for (ItemId market : economy.marketIds()) {
            for (MarketParticipant participant : economy.participants(market)) {
                Company company = registry.get(participant.companyId()).orElse(null);
                if (company == null) {
                    continue;
                }
                LegalFormDef legalForm = company.legalForm(companyParams);

                Key key = new Key(company.id(), market);
                active.add(key);
                Slot slot = slots.get(key);
                if (slot == null || slot.isOutdated(participant, company.legalLevel())) {
                    slot = drawSlot(company, market, participant, legalForm, economy, requestParams);
                    slots.put(key, slot);
                }
                if (slot.never()) {
                    continue;
                }

                slot.countdown -= ticks;
                while (slot.countdown <= 0) {
                    int quantityFactor = RequestRules.drawQuantityFactor(requestParams, rng);
                    double urgency = RequestRules.drawUrgency(requestParams, rng);
                    arrivals.add(arrive(company, economy, market, legalForm, requestParams, quantityFactor, urgency));
                    slot.countdown += RequestRules.drawCountdown(slot.rate, rng); // Counting whatever is left from this countdown towards the next
                }
            }
        }
        slots.keySet().retainAll(active);
        return arrivals;
    }

    public OptionalLong ticksUntilNextRequest(CompanyId company, ItemId market) {
        Slot slot = slots.get(new Key(company, market));
        if (slot == null || slot.never()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(slot.countdown);
    }

    /**
     * Lets one request arrive at a company.
     * @param company The company. Has to be registered in the market.
     * @param economy The economy
     * @param product The product
     * @param legalForm The legal form of the company
     * @param params The request params
     * @param quantityFactor The quantity factor of the request
     * @param urgency The urgency of the request
     * @return What happened
     */
    public static Arrival arrive(Company company,
                                 Economy economy,
                                 ItemId product,
                                 LegalFormDef legalForm,
                                 RequestParams params,
                                 int quantityFactor,
                                 double urgency) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(product, "product must not be null.");
        Objects.requireNonNull(legalForm, "legalForm must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        double listPrice = listPriceOf(company, economy, product);

        RequestBoard board = company.requestBoard();
        if (!board.hasRoom(product, legalForm)) {
            board.recordLostRequest(product);
            return new Arrival(company.id(), product, null);
        }

        int quantity = RequestRules.quantity(quantityFactor, economy.packageSize(product), legalForm.maxOrderQuantity());
        double ticksPerUnit = RequestRules.manufacturingTicksPerUnit(economy.manufacturingDepth(product));
        long deadline = RequestRules.deadlineTicks(quantity, urgency, ticksPerUnit, legalForm.deadlineFactor(), params);
        double unitPrice = RequestRules.unitPrice(listPrice, urgency, quantityFactor, params);
        long offerDuration = RequestRules.offerDurationTicks(urgency, SALES_REP_FACTOR, params);

        Request request = new Request(
                company.issueRequestNumber(),
                product,
                quantity,
                quantityFactor,
                urgency,
                unitPrice,
                deadline,
                offerDuration
        );
        board.add(request, legalForm);
        return new Arrival(company.id(), product, request);
    }

    private Slot drawSlot(Company company,
                          ItemId market,
                          MarketParticipant participant,
                          LegalFormDef legalForm,
                          Economy economy,
                          RequestParams params) {
        double rate = 0.0;
        if (economy.packageSize(market) <= legalForm.maxOrderQuantity()) {
            double averageRequestSize = RequestRules.averageRequestSize(economy.packageSize(market), legalForm.maxOrderQuantity(), params);
            rate = RequestRules.rate(
                    economy.expectedDailyQuantity(market, company.id()),
                    FRAMEWORK_CONTRACT_QUANTITY,
                    economy.overflowShare(market, company.id()),
                    averageRequestSize,
                    participant.listPrice(),
                    economy.marketSnapshot(market).displayedPrice(),
                    params);
        }

        Slot slot = new Slot(rate, participant.listPrice(), participant.reputation(), company.legalLevel());
        if (rate > 0) {
            slot.countdown = RequestRules.drawCountdown(rate, rng);
        }
        return slot;
    }

    private static double listPriceOf(Company company, Economy economy, ItemId product) {
        for (MarketParticipant participant : economy.participants(product)) {
            if (participant.companyId().equals(company.id())) {
                return participant.listPrice();
            }
        }
        throw new IllegalStateException(company.id() + " does not take part in the market of " + product + ".");
    }
}
