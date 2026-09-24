package com.timder.kontor.core.company;

import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderPhase;
import com.timder.kontor.core.company.order.OrderRules;
import com.timder.kontor.core.company.order.OrderSettlementResult;
import com.timder.kontor.core.economy.Economy;

import java.util.*;

public final class CompanyRegistry {
    private final Map<CompanyId, Company> companies = new LinkedHashMap<>();
    private int nextId = 1;

    /**
     * Founds a new company and registers it.
     * @param name The company name
     * @param day The founding day
     * @param owner The founding player
     * @param takeFounderLoan Whether to take the optional founder loan
     * @param policyRate The current policy rate
     * @param params The company params
     * @return The newly founded and registered company
     * @throws IllegalArgumentException If the name is invalid or already taken or the player already belongs to a company
     */
    public Company found(String name, long day, UUID owner, boolean takeFounderLoan, double policyRate, CompanyParams params) {
        Objects.requireNonNull(owner, "owner must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        String validName = Company.requireValidName(name);

        if (findByName(validName).isPresent()) {
            throw new IllegalArgumentException("A company named \"" + validName + "\" already exists.");
        }
        if (companyOf(owner).isPresent()) {
            throw new IllegalArgumentException("This player already belongs to a company.");
        }

        CompanyId id = new CompanyId(nextId);
        Company company = Company.found(id, validName, day, owner, takeFounderLoan, policyRate, params);
        companies.put(id, company);
        nextId++;
        return company;
    }

    public Optional<Company> delete(CompanyId id) {
        Objects.requireNonNull(id, "id must not be null.");
        return Optional.ofNullable(companies.remove(id));
    }

    /**
     * @param id The company id
     * @return The company, if registered
     */
    public Optional<Company> get(CompanyId id) {
        return Optional.ofNullable(companies.get(id));
    }

    /**
     * @param name The name to look up (is compared case-insensitive after stripping whitespace)
     * @return The company with that name (if a company is found)
     */
    public Optional<Company> findByName(String name) {
        Objects.requireNonNull(name, "name must not be null.");
        String normalized = name.strip();
        for (Company company : companies.values()) {
            if (company.name().equalsIgnoreCase(normalized)) {
                return Optional.of(company);
            }
        }
        return Optional.empty();
    }

    /**
     * @param player The player
     * @return The company the player is owner or manager of
     */
    public Optional<Company> companyOf(UUID player) {
        Objects.requireNonNull(player, "player must not be null.");
        for (Company company : companies.values()) {
            if (company.isMember(player)) {
                return Optional.of(company);
            }
        }
        return Optional.empty();
    }

    /**
     * @return All registered companies, in founding order
     */
    public Collection<Company> all() {
        return Collections.unmodifiableCollection(companies.values());
    }

    public int size() {
        return companies.size();
    }

    public List<Company> insolventCompanies(CompanyParams params) {
        Objects.requireNonNull(params, "params must not be null.");
        List<Company> insolvent = new ArrayList<>();
        for (Company company : companies.values()) {
            if (company.isInsolvent(params)) {
                insolvent.add(company);
            }
        }
        return insolvent;
    }

    public Map<CompanyId, CompanyHistoryEntry> settleDay(long day, double policyRate, CompanyParams params) {
        Objects.requireNonNull(params, "params must not be null.");
        Map<CompanyId, CompanyHistoryEntry> entries = new LinkedHashMap<>();
        for (Company company : companies.values()) {
            entries.put(company.id(), CompanyRules.settleDay(company, day, policyRate, Map.of(), params));
            // TODO Maybe notify company members of lost requests.
            company.requestBoard().resetLostRequestsToday();
        }
        return entries;
    }

    public List<BurstOrder> advance(long ticks, long day) {
        List<BurstOrder> burst = new ArrayList<>();
        for (Company company : companies.values()) {
            company.requestBoard().advance(ticks);
            company.orderBook().advance(ticks);

            for (Order order : company.orderBook().allOrders()) {
                if (order.getPhase() == OrderPhase.GRACE_PERIOD && order.remainingGracePeriodTicks() <= 0) {
                    OrderSettlementResult settlement = OrderRules.settleFailed(company, day, order);
                    company.orderBook().remove(order.getNumber());
                    burst.add(new BurstOrder(company.id(), order, settlement));
                }
            }
        }
        return burst;
    }

    /**
     * Applies the reputation loss of every burst order to its market
     * @param bursts The burst orders
     * @param economy The economy
     * @param params The company parameters
     */
    public void applyBurstReputation(List<BurstOrder> bursts, Economy economy, CompanyParams params) {
        Objects.requireNonNull(bursts, "bursts must not be null.");
        Objects.requireNonNull(economy, "economy must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        for (BurstOrder burst : bursts) {
            Company company = get(burst.companyId()).orElseThrow();
            economy.recordOrderFailed(burst.order().getProduct(), burst.companyId(), burst.order(), company.legalForm(params));
        }
    }

    public record BurstOrder(CompanyId companyId, Order order, OrderSettlementResult settlement) {
        public BurstOrder {
            Objects.requireNonNull(companyId, "companyId must not be null.");
            Objects.requireNonNull(order, "order must not be null.");
            Objects.requireNonNull(settlement, "settlement must not be null.");
        }
    }

    public record SaveState(
            List<Company.SaveState> companies,
            int nextId
    ) {
        public SaveState {
            companies = List.copyOf(companies);
            if (nextId < 1) throw new IllegalArgumentException("nextId must be at least 1.");
        }
    }

    public SaveState getSaveState() {
        List<Company.SaveState> states = new ArrayList<>(companies.size());
        for (Company company : companies.values()) {
            states.add(company.getSaveState());
        }
        return new SaveState(states, nextId);
    }

    public static CompanyRegistry restore(SaveState saveState) {
        Objects.requireNonNull(saveState, "saveState must not be null.");
        CompanyRegistry registry = new CompanyRegistry();
        for (Company.SaveState companyState : saveState.companies()) {
            Company company = Company.restore(companyState);
            registry.companies.put(company.id(), company);
        }
        registry.nextId = saveState.nextId();
        return registry;
    }
}
