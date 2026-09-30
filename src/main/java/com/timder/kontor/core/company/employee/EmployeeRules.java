package com.timder.kontor.core.company.employee;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.legalform.LegalFormDef;

import java.util.Objects;

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
     * @return
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
        Employee employee = Employee.hire(company.issueEmployeeNumber(), spec, day);
        company.addEmployee(employee);
        return employee;
    }

    /**
     * Removes an employee from the company.
     * @param company The company
     * @param number The employee number to remove
     * @return The removed employee
     */
    public static Employee dismiss(Company company, long number) {
        Objects.requireNonNull(company, "company must not be null.");
        return company.removeEmployee(number)
                .orElseThrow(() -> new IllegalArgumentException("The company has no employee " + number + "."));
    }

}
