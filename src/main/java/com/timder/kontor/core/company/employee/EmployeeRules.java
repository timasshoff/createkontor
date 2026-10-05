package com.timder.kontor.core.company.employee;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalFormDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EmployeeRules {

    /**
     * The salary of one employee for one day.
     * @param baseSalary The base salary
     * @param legalForm The current legal form
     * @return The salary for this employee for one day
     */
    public static Money dailySalary(Money baseSalary, LegalFormDef legalForm) {
        Objects.requireNonNull(baseSalary, "baseSalary must not be null.");
        Objects.requireNonNull(legalForm, "legalForm must not be null.");
        if (baseSalary.isNegative()) throw new IllegalArgumentException("baseSalary must not be negative.");
        return baseSalary.scaled(legalForm.employeeSalaryFactor());
    }

    /**
     * The one-time bonus for hiring somebody
     * @param spec The role and the base salary
     * @param legalForm The current legal form
     * @param params The company parameters
     * @return The bonus
     */
    public static Money hireBonus(RoleSpec spec, LegalFormDef legalForm, CompanyParams params) {
        Objects.requireNonNull(spec, "spec must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        return dailySalary(spec.baseSalary(), legalForm).scaled(params.hireBonusSalaryMultiple());
    }

    /**
     * What hiring costs right now
     * @param company The company
     * @param spec The role and base salary
     * @param day The current day
     * @param params The company parameters
     * @return The bonus
     */
    public static Money hireCost(Company company, RoleSpec spec, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(spec, "spec must not be null.");
        if (company.hasDeparture(spec.id(), day)) {
            return Money.ZERO;
        }
        return hireBonus(spec, company.legalForm(params), params);
    }

    /**
     * @param company The company
     * @param params The company params
     * @return How many more employees the current legal form of the company allows
     */
    public static int freeSlots(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        return Math.max(0, company.legalForm(params).maxEmployees() - company.employeeCount());
    }

    /**
     * Hires an employee.
     * @param company The company
     * @param spec The role specification
     * @param day The day of the hiring
     * @param params The company parameters
     * @return The hired employee
     */
    public static Employee hire(Company company, RoleSpec spec, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(spec, "spec must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        if (freeSlots(company, params) <= 0) {
            throw new IllegalStateException("The company has no free employee slot.");
        }
        if (!company.isOperational()) {
            throw new IllegalStateException("The company is in payment difficulties and cannot hire.");
        }
        Money bonus = hireCost(company, spec, day, params);
        if (bonus.isPositive() && !company.canSpend(bonus, params)) {
            throw new IllegalStateException("The company cannot afford the hiring bonus of " + bonus + ".");
        }
        Employee employee = Employee.hire(company.issueEmployeeNumber(), spec, day);
        company.addEmployee(employee);
        company.consumeDeparture(spec.id(), day);
        if (bonus.isPositive() && !company.trySpend(day, BookingKind.HIRE_BONUS, bonus, spec.id().value(), params)) {
            throw new IllegalStateException("The company could pay the hiring bonus of " + bonus + " a moment ago but cannot now.");
        }
        return employee;
    }

    /**
     * Removes an employee from the company.
     * @param company The company
     * @param number The employee number to remove
     * @param day The current day
     * @return The removed employee
     */
    public static Employee dismiss(Company company, long number, long day) {
        Objects.requireNonNull(company, "company must not be null.");
        Employee employee = company.employee(number)
                .orElseThrow(() -> new IllegalArgumentException("The company has no employee " + number + "."));
        Departure departure = new Departure(employee, day); // validates the day before anything changes
        company.removeEmployee(number);
        company.addDeparture(departure);
        return employee;
    }

    /**
     * Stores whether an employee is present or not.
     * @param company The company
     * @param number The employee number
     * @param present Whether the employee is present or not
     * @param day The current day
     * @return The employee with the new state
     */
    public static Employee reportPresence(Company company, long number, boolean present, long day) {
        Objects.requireNonNull(company, "company must not be null.");
        Employee employee = company.employee(number)
                .orElseThrow(() -> new IllegalArgumentException("The company has no employee " + number + "."));
        Employee updated = employee.withPresence(present, day);
        company.replaceEmployee(updated);
        return updated;
    }

    /**
     * Whether the contract of an employee ends
     * @param employee The employee
     * @param day The day of the check
     * @param orphanDays The days without a report after which the contract ends
     * @return The end reason. Empty if the contract continues
     */
    public static Optional<EmployeeEvent.EndReason> endReason(Employee employee, long day, int orphanDays) {
        Objects.requireNonNull(employee, "employee must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (orphanDays < 1) throw new IllegalArgumentException("orphanDays must be at least 1.");

        if (!employee.present()) {
            return Optional.of(EmployeeEvent.EndReason.ABSENT);
        }
        if (day - employee.lastReportDay() >= orphanDays) {
            return Optional.of(EmployeeEvent.EndReason.NOT_REPORTING);
        }
        return Optional.empty();
    }

    /**
     * Ends the contract of every employee that qualified for termination
     *
     * @param company The company
     * @param day The current day
     * @param orphanDays The days without a report after which the contract ends
     * @return One event per ended contract
     */
    public static List<EmployeeEvent> endContracts(Company company, long day, int orphanDays) {
        Objects.requireNonNull(company, "company must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (orphanDays < 1) throw new IllegalArgumentException("orphanDays must be at least 1.");
        List<EmployeeEvent> events = new ArrayList<>();
        for (Employee employee : company.employees()) {
            Optional<EmployeeEvent.EndReason> reason = endReason(employee, day, orphanDays);
            if (reason.isPresent()) {
                company.removeEmployee(employee.number());
                events.add(new EmployeeEvent.ContractEnded(company.id(), employee, reason.get()));
            }
        }
        return events;
    }
}
