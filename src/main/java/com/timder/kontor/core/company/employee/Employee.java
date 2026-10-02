package com.timder.kontor.core.company.employee;

import com.timder.kontor.core.company.financial.Money;

import java.util.Objects;

public record Employee(
        long number,
        RoleId role,
        Money baseSalary,
        long hiredDay,
        boolean present,
        long lastReportDay
) {

    public Employee {
        if (number < 1) throw new IllegalArgumentException("number must be at least 1.");
        Objects.requireNonNull(role, "role must not be null.");
        Objects.requireNonNull(baseSalary, "baseSalary must not be null.");
        if (baseSalary.isNegative()) throw new IllegalArgumentException("baseSalary must not be negative.");
        if (hiredDay < 0) throw new IllegalArgumentException("hiredDay must not be negative.");
        if (lastReportDay < hiredDay) throw new IllegalArgumentException("lastReportDay must not be before hiredDay.");
    }

    public static Employee hire(long number, RoleSpec spec, long day) {
        Objects.requireNonNull(spec, "spec must not be null.");
        return new Employee(number, spec.id(), spec.baseSalary(), day, true, day);
    }

    public Employee withPresence(boolean present, long day) {
        if (day < lastReportDay) throw new IllegalArgumentException("day must not be before the last report.");
        return new Employee(number, role, baseSalary, hiredDay, present, day);
    }

}
