package com.timder.kontor.core.company.employee;

import java.util.Objects;

public record Departure(Employee employee, long day) {

    public Departure {
        Objects.requireNonNull(employee, "employee must not be null.");
        if (day < employee.hiredDay()) {
            throw new IllegalArgumentException("day must not be before the day of hiring (" + employee.hiredDay() + ").");
        }
    }
}
