package com.timder.kontor.core.company;

import com.timder.kontor.core.company.employee.Departure;
import com.timder.kontor.core.company.employee.Employee;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.core.company.financial.*;
import com.timder.kontor.core.company.legalform.LegalFormDef;
import com.timder.kontor.core.company.legalform.LegalForms;
import com.timder.kontor.core.company.legalform.UpgradeApplication;
import com.timder.kontor.core.company.license.LicenseHolding;
import com.timder.kontor.core.company.license.LicenseKey;
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

    public static final long NEVER_ACTIVE = -1L;

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

    private long nextRequestNumber = 1;
    private long nextOrderNumber = 1;

    private long fulfilledOrders = 0;

    private UpgradeApplication upgradeApplication = null;

    private final Map<LicenseKey, LicenseHolding> licenses = new LinkedHashMap<>();

    private long nextEmployeeNumber = 1;
    private final Map<Long, Employee> employees = new LinkedHashMap<>();
    private final List<Departure> departures = new ArrayList<>();

    private final Deque<CompanyHistoryEntry> history = new ArrayDeque<>();

    private final Map<String, Integer> boundResourceCounts = new LinkedHashMap<>();

    private long lastActiveDay = NEVER_ACTIVE;
    private boolean active = false;
    private long activeTicks = 0;

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

        company.markActive(day);
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
     * A company is active while at least one member is online.
     * @return True if the company is active right now
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return The last day on which the company was active
     */
    public long lastActiveDay() {
        return lastActiveDay;
    }

    /**
     * Whether a day is settled for this company: only if the company was active on it.
     * @param day The day that is being closed
     * @return True if the last active day is this day
     */
    public boolean isActiveDay(long day) {
        return day >= 0 && lastActiveDay == day;
    }

    /**
     * How long the company was active on one day
     * @param day The day
     * @return How often markActive was called on that day
     */
    public long activeTicksOn(long day) {
        return isActiveDay(day) ? activeTicks : 0;
    }

    /**
     * Records that a member is online right now.
     * @param day The current day, not before the last active day
     * @throws IllegalArgumentException if the day is negative or before the last active day
     */
    public void markActive(long day) {
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (day < lastActiveDay) throw new IllegalArgumentException("day " + day + " is before the last active day " + lastActiveDay + ".");
        if (day != lastActiveDay) {
            this.activeTicks = 0;
        }
        this.active = true;
        this.lastActiveDay = day;
        if (activeTicks < Long.MAX_VALUE) {
            this.activeTicks++;
        }
    }

    /**
     * Records that no member is online right now
     */
    public void markInactive() {
        this.active = false;
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
     * Directly sets the company's legal level and removes any pending application.
     * @param level The new legal level, must be a valid level in {@code legalForms}
     * @param legalForms The legal forms of this world, used only to validate the level
     * @throws IllegalArgumentException if level is not a valid level in legalForms
     */
    public void setLegalLevel(int level, LegalForms legalForms) {
        Objects.requireNonNull(legalForms, "legalForms must not be null.");
        legalForms.get(level);
        this.legalLevel = level;
        if (upgradeApplication != null && upgradeApplication.targetLevel() != level + 1) {
            upgradeApplication = null;
        }
    }

    /**
     * @return The running application for a higher legal form. Empty if there is none
     */
    public Optional<UpgradeApplication> upgradeApplication() {
        return Optional.ofNullable(upgradeApplication);
    }

    /**
     * Starts an application.
     * @param application The application
     * @throws IllegalStateException if the company already has an application
     * @throws IllegalArgumentException if the application does not aim at the next level
     */
    public void startUpgrade(UpgradeApplication application) {
        Objects.requireNonNull(application, "application must not be null.");
        if (upgradeApplication != null) throw new IllegalStateException("The company already has an application.");
        if (application.targetLevel() != legalLevel + 1) {
            throw new IllegalArgumentException("A company of level " + legalLevel + " can only apply for level " + (legalLevel + 1) + ", not " + application.targetLevel() + ".");
        }
        this.upgradeApplication = application;
    }

    /**
     * Replaces the running application by its next state (e.g. fewer ticks or resting).
     * Target must stay the same.
     * @param application The new application state
     */
    public void replaceUpgrade(UpgradeApplication application) {
        Objects.requireNonNull(application, "application must not be null.");
        if (upgradeApplication == null) throw new IllegalStateException("The company has no application.");

        if (application.targetLevel() != upgradeApplication.targetLevel()) {
            throw new IllegalArgumentException("The target level of a running application cannot change.");
        }

        this.upgradeApplication = application;
    }

    /**
     * Removes the running application.
     * @return True if there was an application to remove.
     */
    public boolean discardUpgrade() {
        boolean present = upgradeApplication != null;
        upgradeApplication = null;
        return present;
    }

    /**
     * @return All current licenses
     */
    public List<LicenseHolding> licenses() {
        return List.copyOf(licenses.values());
    }

    /**
     * @param key The license key
     * @return The holding of that license. Empty if the company does not own it.
     */
    public Optional<LicenseHolding> license(LicenseKey key) {
        Objects.requireNonNull(key, "key must not be null.");
        return Optional.ofNullable(licenses.get(key));
    }

    /**
     * Adds a newly bought license
     * @param holding The new holding, must be active
     * @throws IllegalStateException if the company already owns the license
     * @throws IllegalArgumentException if the holding is cancelled
     */
    public void addLicense(LicenseHolding holding) {
        Objects.requireNonNull(holding, "holding must not be null.");
        if (holding.cancelled()) throw new IllegalArgumentException("A new license must not be cancelled.");
        if (licenses.containsKey(holding.key())) {
            throw new IllegalStateException("The company already owns the license " + holding.key() + ".");
        }
        licenses.put(holding.key(), holding);
    }

    /**
     * Replaces a license the company owns by a changed version of it
     * @param holding The changed holding
     * @throws IllegalStateException if the company does not own the license
     */
    public void replaceLicense(LicenseHolding holding) {
        Objects.requireNonNull(holding, "holding must not be null.");
        if (!licenses.containsKey(holding.key())) {
            throw new IllegalStateException("The company does not own the license " + holding.key() + ".");
        }
        licenses.put(holding.key(), holding);
    }

    /**
     * @param key The license
     * @return True if the company owned the license and it was removed.
     */
    public boolean removeLicense(LicenseKey key) {
        Objects.requireNonNull(key, "key must not be null.");
        return licenses.remove(key) != null;
    }

    public List<Employee> employees() {
        return List.copyOf(employees.values());
    }

    public int employeeCount() {
        return employees.size();
    }

    public Optional<Employee> employee(long number) {
        return Optional.ofNullable(employees.get(number));
    }

    public long issueEmployeeNumber() {
        return nextEmployeeNumber++;
    }

    /**
     * Adds a newly hired employee
     * @param employee The employee to add
     */
    public void addEmployee(Employee employee) {
        Objects.requireNonNull(employee, "employee must not be null.");
        if (employee.number() >= nextEmployeeNumber) {
            throw new IllegalArgumentException("The employee number " + employee.number() + " was not issued.");
        }
        if (employees.containsKey(employee.number())) {
            throw new IllegalStateException("The company already has the employee " + employee.number() + ".");
        }
        employees.put(employee.number(), employee);
    }

    /**
     * Replaces an employee with a changed version of the same employee
     * @param employee The employee
     */
    public void replaceEmployee(Employee employee) {
        Objects.requireNonNull(employee, "employee must not be null.");
        if (!employees.containsKey(employee.number())) {
            throw new IllegalStateException("The company has no employee " + employee.number() + ".");
        }
        employees.put(employee.number(), employee);
    }

    public Optional<Employee> removeEmployee(long number) {
        return Optional.ofNullable(employees.remove(number));
    }

    /**
     * @return The dismissed employees whose salary for the day of dismissal is still to be booked
     */
    public List<Departure> departures() {
        return List.copyOf(departures);
    }

    /**
     * Remembers a dismissed employee
     * @param departure The departure
     */
    public void addDeparture(Departure departure) {
        Objects.requireNonNull(departure, "departure must not be null.");
        if (employees.containsKey(departure.employee().number())) {
            throw new IllegalStateException("The employee " + departure.employee().number() + " still works for the company.");
        }
        departures.add(departure);
    }

    public boolean hasDeparture(RoleId role, long day) {
        Objects.requireNonNull(role, "role must not be null.");
        for (Departure departure : departures) {
            if (departure.day() == day && departure.employee().role().equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the oldest departure of a role on a day
     * @param role The role
     * @param day The day
     * @return True if a departure was removed
     */
    public boolean consumeDeparture(RoleId role, long day) {
        Objects.requireNonNull(role, "role must not be null.");
        Iterator<Departure> iterator = departures.iterator();
        while (iterator.hasNext()) {
            Departure departure = iterator.next();
            if (departure.day() == day && departure.employee().role().equals(role)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Forgets every departure up to and including a day
     * @param day The day
     */
    public void dropDeparturesUpTo(long day) {
        departures.removeIf(departure -> departure.day() <= day);
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
        if (isMember(player)) throw new IllegalArgumentException("Player is already a member.");
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

    public long issueRequestNumber() {
        return nextRequestNumber++;
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
        requestBoard.get(requestNumber);

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

    public long fulfilledOrders() {
        return fulfilledOrders;
    }

    public void recordFulfilledOrder() {
        if (fulfilledOrders < Long.MAX_VALUE) {
            fulfilledOrders++;
        }
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

    /**
     * @param resourceKey Key identifying the bound resource
     * @return How many of that resource this company currently has bound
     */
    public int boundResourceCount(String resourceKey) {
        Objects.requireNonNull(resourceKey, "resourceKey must not be null.");
        return boundResourceCounts.getOrDefault(resourceKey, 0);
    }

    /**
     * Records one more of a counted resource bound to this company.
     * @param resourceKey The key identifying the counted resource
     */
    public void bindResource(String resourceKey) {
        Objects.requireNonNull(resourceKey, "resourceKey must not be null.");
        boundResourceCounts.merge(resourceKey, 1, Integer::sum);
    }

    /**
     * Records one less of a counted resource bound to this company.
     * @param resourceKey The key identifying the counted resource
     */
    public void unbindResource(String resourceKey) {
        Objects.requireNonNull(resourceKey, "resourceKey must not be null.");
        boundResourceCounts.computeIfPresent(resourceKey, (key, count) -> count <= 1 ? null : count - 1);
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
            long nextRequestNumber,
            long nextOrderNumber,
            List<CompanyHistoryEntry> history,
            RequestBoard.SaveState requestBoard,
            OrderBook.SaveState orderBook,
            Map<String, Integer> boundResourceCounts,
            long fulfilledOrders,
            UpgradeApplication upgradeApplication,
            List<LicenseHolding> licenses,
            long nextEmployeeNumber,
            List<Employee> employees,
            long lastActiveDay,
            List<Departure> departures
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
            boundResourceCounts = boundResourceCounts == null ? Map.of() : Map.copyOf(boundResourceCounts);
            if (fulfilledOrders < 0) throw new IllegalArgumentException("fulfilledOrders must not be negative.");

            if (upgradeApplication != null && upgradeApplication.targetLevel() != legalLevel + 1) {
                throw new IllegalArgumentException("upgradeApplication must aim at level " + (legalLevel + 1) + ".");
            }

            licenses = licenses == null ? List.of() : List.copyOf(licenses);
            Set<LicenseKey> keys = new HashSet<>();
            for (LicenseHolding holding : licenses) {
                if (!keys.add(holding.key())) throw new IllegalArgumentException("licenses contains " + holding.key() + " twice.");
            }

            if (nextEmployeeNumber < 1) throw new IllegalArgumentException("nextEmployeeNumber must be at least 1.");
            employees = employees == null ? List.of() : List.copyOf(employees);
            Set<Long> numbers = new HashSet<>();
            for (Employee employee : employees) {
                if (!numbers.add(employee.number())) throw new IllegalArgumentException("employees contains number " + employee.number() + " twice.");
                if (employee.number() >= nextEmployeeNumber) {
                    throw new IllegalArgumentException("employee number " + employee.number() + " is not below nextEmployeeNumber.");
                }
            }

            if (lastActiveDay < NEVER_ACTIVE) throw new IllegalArgumentException("lastActiveDay must be at least " + NEVER_ACTIVE + ".");

            departures = departures == null ? List.of() : List.copyOf(departures);
            for (Departure departure : departures) {
                if (numbers.contains(departure.employee().number())) {
                    throw new IllegalArgumentException("employee " + departure.employee().number() + " is both employed and departed.");
                }
            }
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
                nextRequestNumber,
                nextOrderNumber,
                List.copyOf(history),
                requestBoard.getSaveState(),
                orderBook.getSaveState(),
                boundResourceCounts,
                fulfilledOrders,
                upgradeApplication,
                List.copyOf(licenses.values()),
                nextEmployeeNumber,
                List.copyOf(employees.values()),
                lastActiveDay,
                List.copyOf(departures));
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
        company.nextRequestNumber = saveState.nextRequestNumber();
        company.nextOrderNumber = saveState.nextOrderNumber();
        company.history.addAll(saveState.history());
        company.boundResourceCounts.putAll(saveState.boundResourceCounts());
        company.fulfilledOrders = saveState.fulfilledOrders();
        company.upgradeApplication = saveState.upgradeApplication();
        for (LicenseHolding holding : saveState.licenses()) {
            company.licenses.put(holding.key(), holding);
        }
        company.nextEmployeeNumber = saveState.nextEmployeeNumber();
        for (Employee employee : saveState.employees()) {
            company.employees.put(employee.number(), employee);
        }
        company.lastActiveDay = saveState.lastActiveDay();
        company.departures.addAll(saveState.departures());
        return company;
    }
}
