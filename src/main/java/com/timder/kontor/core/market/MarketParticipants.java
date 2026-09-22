package com.timder.kontor.core.market;

import com.timder.kontor.core.company.CompanyId;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MarketParticipants {

    private final Map<CompanyId, MarketParticipant> byCompany = new LinkedHashMap<>();

    public static MarketParticipants empty() {
        return new MarketParticipants();
    }

    /**
     * Registers a company as a new participant of this market
     * @param companyId The company
     * @param listPrice The list price of the product
     * @param reputation The starting reputation (0 to 100)
     */
    public void register(CompanyId companyId, double listPrice, double reputation) {
        if (companyId == null) throw new IllegalArgumentException("companyId must not be null.");
        if (byCompany.containsKey(companyId)) {
            throw new IllegalStateException(companyId + " is already registered on this market.");
        }
        byCompany.put(companyId, new MarketParticipant(companyId, listPrice, reputation));
    }

    public void updateListPrice(CompanyId companyId, double listPrice) {
        MarketParticipant current = require(companyId);
        byCompany.put(companyId, new MarketParticipant(companyId, listPrice, current.reputation()));
    }

    public void updateReputation(CompanyId companyId, double reputation) {
        MarketParticipant current = require(companyId);
        byCompany.put(companyId, new MarketParticipant(companyId, current.listPrice(), reputation));
    }

    /**
     * Removes a company from this market.
     * @param companyId The company to remove
     */
    public void withdraw(CompanyId companyId) {
        byCompany.remove(companyId);
    }

    /**
     * Whether a company currently takes part in this market.
     * @param companyId The company
     * @return True, if registered
     */
    public boolean isRegistered(CompanyId companyId) {
        return byCompany.containsKey(companyId);
    }

    public MarketParticipant get(CompanyId companyId) {
        return require(companyId);
    }

    public Collection<MarketParticipant> all() {
        return List.copyOf(byCompany.values());
    }

    private MarketParticipant require(CompanyId companyId) {
        MarketParticipant participant = byCompany.get(companyId);
        if (participant == null) {
            throw new IllegalArgumentException(companyId + " is not registered on this market.");
        }
        return participant;
    }

    public record SaveState(Map<CompanyId, MarketParticipant> participants) {
        public SaveState {
            participants = Map.copyOf(participants);
        }
    }

    public SaveState getSaveState() {
        return new SaveState(byCompany);
    }

    public static MarketParticipants restore(SaveState saveState) {
        MarketParticipants participants = new MarketParticipants();
        participants.byCompany.putAll(saveState.participants());
        return participants;
    }

    @Override
    public String toString() {
        return "MarketParticipants" + byCompany.values();
    }

}
