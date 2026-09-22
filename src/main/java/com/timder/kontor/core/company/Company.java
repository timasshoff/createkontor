package com.timder.kontor.core.company;

import com.timder.kontor.core.company.financial.*;

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
    private final List<Loan> loans = new ArrayList<>();
    private Liquidity liquidity = Liquidity.NORMAL;
    private int daysInTrouble = 0;

    private final Deque<CompanyHistoryEntry> history = new ArrayDeque<>();

    private Company(CompanyId id, String name, long foundingDay, UUID owner, Account account) {
        this.id = id;
        this.name = name;
        this.foundingDay = foundingDay;
        this.owner = owner;
        this.account = account;
        this.legalLevel = 1;
    }

    public static Company found(CompanyId id, String name, long day, UUID owner, boolean takeFounderLoan, double policyRate, CompanyParams params) {
        Objects.requireNonNull(id, "id must not be null.");
        Objects.requireNonNull(owner, "owner must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        String validName = requireValidName(name);
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (takeFounderLoan && !params.founderLoan().isPositive()) throw new IllegalArgumentException("The settings do not offer a founder loan.");

        Company company = new Company(id, validName, day, owner, Account.empty());

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
}
