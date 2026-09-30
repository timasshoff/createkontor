package com.timder.kontor.core.market;

import com.timder.kontor.core.company.CompanyId;

import java.util.*;

public final class MarketParticipants {

    private final Map<CompanyId, MarketParticipant> byCompany = new LinkedHashMap<>();
    private final Map<CompanyId, MarketParticipant> paused = new LinkedHashMap<>();

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
        if (paused.containsKey(companyId)) {
            throw new IllegalStateException(companyId + " is paused on this market and has to resume instead.");
        }
        byCompany.put(companyId, new MarketParticipant(companyId, listPrice, reputation));
    }

    public void updateListPrice(CompanyId companyId, double listPrice) {
        MarketParticipant current = require(companyId);
        byCompany.put(companyId, new MarketParticipant(companyId, listPrice, current.reputation()));
    }

    public void updateReputation(CompanyId companyId, double reputation) {
        MarketParticipant current = byCompany.get(companyId);
        if (current != null) {
            byCompany.put(companyId, new MarketParticipant(companyId, current.listPrice(), reputation));
            return;
        }
        current = paused.get(companyId);
        if (current == null) {
            throw new IllegalArgumentException(companyId + " is not registered on this market.");
        }
        paused.put(companyId, new MarketParticipant(companyId, current.listPrice(), reputation));
    }

    /**
     * Removes a company from this market.
     * @param companyId The company to remove
     */
    public void withdraw(CompanyId companyId) {
        byCompany.remove(companyId);
        paused.remove(companyId);
    }

    /**
     * Pauses an active company
     * @param companyId The company to pause
     */
    public void pause(CompanyId companyId) {
        MarketParticipant current = byCompany.remove(companyId);
        if (current == null) {
            throw new IllegalStateException(companyId + " is not registered on this market.");
        }
        paused.put(companyId, current);
    }

    /**
     * Resumes an active company
     * @param companyId The company to resume
     */
    public void resume(CompanyId companyId) {
        MarketParticipant current = paused.remove(companyId);
        if (current == null) {
            throw new IllegalStateException(companyId + " is not paused on this market.");
        }
        byCompany.put(companyId, current);
    }

    public boolean isPaused(CompanyId companyId) {
        return paused.containsKey(companyId);
    }

    /**
     * @param companyId The company
     * @return The company's entry, whether active or paused. Empty if the market does not know the company.
     */
    public Optional<MarketParticipant> stored(CompanyId companyId) {
        MarketParticipant current = byCompany.get(companyId);
        return Optional.ofNullable(current != null ? current : paused.get(companyId));
    }

    public Collection<MarketParticipant> pausedAll() {
        return List.copyOf(paused.values());
    }

    public Collection<MarketParticipant> allStored() {
        List<MarketParticipant> result = new ArrayList<>(byCompany.values());
        result.addAll(paused.values());
        return List.copyOf(result);
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

    public record SaveState(Map<CompanyId, MarketParticipant> participants, Map<CompanyId, MarketParticipant> paused) {
        public SaveState {
            participants = Collections.unmodifiableMap(new LinkedHashMap<>(participants));
            paused = Collections.unmodifiableMap(new LinkedHashMap<>(paused));
            for (CompanyId companyId : paused.keySet()) {
                if (participants.containsKey(companyId)) {
                    throw new IllegalArgumentException(companyId + " cannot be active and paused at once.");
                }
            };
        }

        public SaveState(Map<CompanyId, MarketParticipant> participants) {
            this(participants, Map.of());
        }
    }

    public SaveState getSaveState() {
        return new SaveState(byCompany, paused);
    }

    public static MarketParticipants restore(SaveState saveState) {
        MarketParticipants participants = new MarketParticipants();
        participants.byCompany.putAll(saveState.participants());
        participants.paused.putAll(saveState.paused());
        return participants;
    }

    @Override
    public String toString() {
        return "MarketParticipants" + byCompany.values() + " paused" + paused.values();
    }

}
