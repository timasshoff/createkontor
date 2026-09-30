package com.timder.kontor.core.company.employee;

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

}
