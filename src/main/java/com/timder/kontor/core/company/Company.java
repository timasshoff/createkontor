package com.timder.kontor.core.company;

import com.timder.kontor.core.company.financial.*;
import com.timder.kontor.core.company.order.Order;
import com.timder.kontor.core.company.order.OrderBook;
import com.timder.kontor.core.company.order.RequestOrigin;
import com.timder.kontor.core.company.request.Request;
import com.timder.kontor.core.company.request.RequestBoard;
import com.timder.kontor.core.company.request.RequestParams;
import com.timder.kontor.core.company.request.RequestRules;

import java.util.*;

public final class Company {

    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 32;

    public static final String FOUNDER_LOAN_REFERENCE = "founder_loan";

    private final CompanyId id;
    private final String name;
    private final long foundingDay;
    private final UUID owner;
    private final Set<UUID> managers = new LinkedHashSet<>();

    private int legalLevel;
    private final Account account;
    private final RequestBoard requestBoard;
    private final OrderBook orderBook;
    private final List<Loan> loans = new ArrayList<>();
    private Liquidity liquidity = Liquidity.NORMAL;
    private int daysInTrouble = 0;

    private long nextOrderNumber = 1;

    private final Deque<CompanyHistoryEntry> history = new ArrayDeque<>();

    private Company(CompanyId id, String name, long foundingDay, UUID owner, Account account, RequestBoard requestBoard, OrderBook orderBook) {
        this.id = id;
        this.name = name;
        this.foundingDay = foundingDay;
        this.owner = owner;
        this.account = account;
        this.legalLevel = 1;
        this.requestBoard = requestBoard;
        this.orderBook = orderBook;
    }

    public static Company found(CompanyId id, String name, long day, UUID owner, boolean takeFounderLoan, double policyRate, CompanyParams params) {
        Objects.requireNonNull(id, "id must not be null.");
        Objects.requireNonNull(owner, "owner must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        String validName = requireValidName(name);
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (takeFounderLoan && !params.founderLoan().isPositive()) throw new IllegalArgumentException("The settings do not offer a founder loan.");

        Company company = new Company(id, validName, day, owner, Account.empty(), new RequestBoard(), new OrderBook());

        if (params.startDeposit().isPositive()) {
            company.account.book(day, BookingKind.DEPOSIT, params.startDeposit(), "");
        }

        if (takeFounderLoan) {
            Loan loan = Loan.founder(params.founderLoan(), policyRate, params);
            company.account.book(day, BookingKind.LOAN_PAYOUT, loan.principal(), FOUNDER_LOAN_REFERENCE);
            company.loans.add(loan);
        }

        return company;
    }

    public static String requireValidName(String name) {
        Objects.requireNonNull(name, "name must not be null.");
        String stripped = name.strip();
        int length = stripped.codePointCount(0, stripped.length());
        if (length < MIN_NAME_LENGTH || length > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name must have " + MIN_NAME_LENGTH + " to " + MAX_NAME_LENGTH + " characters.");
        }
        if (stripped.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("name must not contain control characters.");
        }
        return stripped;
    }

    public CompanyId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long foundingDay() {
        return foundingDay;
    }

    /**
     * @return The level of the legal form
     */
    public int legalLevel() {
        return legalLevel;
    }

    /**
     * @param params The company parameters
     * @return The limits and unlocks of the current legal form
     */
    public LegalFormDef legalForm(CompanyParams params) {
        return params.legalForms().get(legalLevel);
    }

    /**
     * @param params The company parameters
     * @return The overdraft limit of the current legal form
     */
    public Money overdraftLimit(CompanyParams params) {
        return legalForm(params).overdraftLimit();
    }

    public UUID owner() {
        return owner;
    }

    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    public Set<UUID> managers() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(managers));
    }

    /**
     * @param player The player
     * @return True if the player is the owner or a manager
     */
    public boolean isMember(UUID player) {
        return isOwner(player) || managers.contains(player);
    }

    public void addManager(UUID player) {
        Objects.requireNonNull(player, "player must not be null.");
        if (isOwner(player)) throw new IllegalArgumentException("The owner is no manager.");
        managers.add(player);
    }

    public boolean removeManager(UUID player) {
        return managers.remove(player);
    }

    public Account account() {
        return account;
    }

    public RequestBoard requestBoard() {
        return requestBoard;
    }

    public OrderBook orderBook() {
        return orderBook;
    }

    /**
     * Accepts an open request and turns it into an order.
     * The order is automatically added to the order book.
     * @param requestNumber The number of the request to accept
     * @param companyParams The company parameters
     * @param requestParams The request parameters
     * @return The accepted newly created order
     */
    public Order acceptRequest(long requestNumber, CompanyParams companyParams, RequestParams requestParams) {
        LegalFormDef legalForm = legalForm(companyParams);
        if (!orderBook.hasRoom(legalForm)) {
            throw new IllegalStateException("The order book has no room.");
        }

        Request request = requestBoard.accept(requestNumber);
        long gracePeriodTicks = RequestRules.gracePeriodTicks(request.getDeadlineTicks(), requestParams);
        Order order = Order.fromRequest(nextOrderNumber, request, gracePeriodTicks, new RequestOrigin(request.getNumber()));
        nextOrderNumber++;
        orderBook.add(order, legalForm);
        return order;
    }

    /**
     * @return A copy of the loans that are not repaid yet
     */
    public List<Loan> loans() {
        return List.copyOf(loans);
    }

    public Money totalDebt() {
        Money debt = Money.ZERO;
        for (Loan loan : loans) {
            debt = debt.plus(loan.outstanding());
        }
        return debt;
    }

    public Money netWorth() {
        return account.getBalance().minus(totalDebt());
    }

    public Liquidity liquidity() {
        return liquidity;
    }

    /**
     * Amount of days the company has been in trouble (= illiquid)
     * @return The amount of days
     */
    public int daysInTrouble() {
        return daysInTrouble;
    }

    public boolean isInsolvent(CompanyParams params) {
        return params.insolvencyEnabled() && daysInTrouble >= params.insolvencyDays();
    }

    public boolean isOperational() {
        return liquidity == Liquidity.NORMAL;
    }

    /**
     * Whether the company could spend an amount right now (both affordable and company is operational).
     * @param cost The amount
     * @param params The company parameters
     * @return True if a matching call to {@link #trySpend} would succeed.
     */
    public boolean canSpend(Money cost, CompanyParams params) {
        Objects.requireNonNull(params, "params must not be null.");
        if (!cost.isPositive()) {
            return false;
        }
        boolean affordable = account.canAfford(cost, overdraftLimit(params));
        return affordable && isOperational();
    }

    /**
     * Spends money a company chooses to spend on its own initiativ (e.g. a purchase).
     * Pays and books a cost, if the company is able to spend it.
     * @param day
     * @param kind
     * @param cost
     * @param reference
     * @param params
     * @return
     */
    public boolean trySpend(long day, BookingKind kind, Money cost, String reference, CompanyParams params) {
        Objects.requireNonNull(params, "params must not be null.");
        if (!canSpend(cost, params)) {
            return false;
        }
        return account.tryDebit(day, kind, cost, reference, overdraftLimit(params));
    }

    public List<CompanyHistoryEntry> history() {
        return List.copyOf(history);
    }

    void setLiquidity(Liquidity liquidity) {
        this.liquidity = Objects.requireNonNull(liquidity, "liquidity must not be null.");
    }

    void setDaysInTrouble(int daysInTrouble) {
        if (daysInTrouble < 0) throw new IllegalArgumentException("daysInTrouble must not be negative.");
        this.daysInTrouble = daysInTrouble;
    }

    void removeLoan(Loan loan) {
        loans.remove(loan);
    }

    void appendHistory(CompanyHistoryEntry entry, int maxLength) {
        history.addLast(entry);
        if (history.size() > maxLength) {
            history.removeFirst();
        }
    }

    @Override
    public String toString() {
        return "Company[%s, \"%s\", level %d, balance=%s, status=%s]".formatted(id, name, legalLevel, account.getBalance(), liquidity);
    }

    public record SaveState(
            CompanyId id,
            String name,
            long foundingDay,
            UUID owner,
            Set<UUID> managers,
            int legalLevel,
            Account.SaveState account,
            List<Loan.SaveState> loans,
            Liquidity liquidity,
            int daysInTrouble,
            long nextOrderNumber,
            List<CompanyHistoryEntry> history,
            RequestBoard.SaveState requestBoard,
            OrderBook.SaveState orderBook
    ) {
        public SaveState {
            Objects.requireNonNull(id, "id must not be null.");
            Objects.requireNonNull(name, "name must not be null.");
            Objects.requireNonNull(owner, "owner must not be null.");
            Objects.requireNonNull(account, "account must not be null.");
            Objects.requireNonNull(liquidity, "liquidity must not be null.");
            Objects.requireNonNull(requestBoard, "requestBoard must not be null.");
            Objects.requireNonNull(orderBook, "orderBook must not be null.");
            if (foundingDay < 0) throw new IllegalArgumentException("foundingDay must not be negative.");
            if (legalLevel < 1) throw new IllegalArgumentException("legalLevel must be at least 1.");
            if (daysInTrouble < 0) throw new IllegalArgumentException("daysInTrouble must not be negative.");
            managers = Set.copyOf(managers);
            if (managers.contains(owner)) throw new IllegalArgumentException("owner must not be a manager.");
            loans = List.copyOf(loans);
            history = List.copyOf(history);
        }
    }

    public SaveState getSaveState() {
        List<Loan.SaveState> loanStates = new ArrayList<>(loans.size());
        for (Loan loan : loans) {
            loanStates.add(loan.getSaveState());
        }
        return new SaveState(
                id,
                name,
                foundingDay,
                owner,
                managers,
                legalLevel,
                account.getSaveState(),
                loanStates,
                liquidity,
                daysInTrouble,
                nextOrderNumber,
                List.copyOf(history),
                requestBoard.getSaveState(),
                orderBook.getSaveState());
    }

    public static Company restore(SaveState saveState) {
        Company company = new Company(
                saveState.id(),
                saveState.name(),
                saveState.foundingDay(),
                saveState.owner(),
                Account.restore(saveState.account()),
                RequestBoard.restore(saveState.requestBoard()),
                OrderBook.restore(saveState.orderBook()));
        company.managers.addAll(saveState.managers());
        company.legalLevel = saveState.legalLevel();
        for (Loan.SaveState loanState : saveState.loans()) {
            company.loans.add(Loan.restore(loanState));
        }
        company.liquidity = saveState.liquidity();
        company.daysInTrouble = saveState.daysInTrouble();
        company.nextOrderNumber = saveState.nextOrderNumber();
        company.history.addAll(saveState.history());
        return company;
    }
}
