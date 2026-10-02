package com.timder.kontor.core.company.employee;

import java.util.regex.Pattern;

public record RoleId(String value) {

    private static final Pattern VALID = Pattern.compile("[a-z][a-z0-9]*(_[a-z0-9]+)*");

    public RoleId {
        if (value == null || !VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("value must be lower snake_case, but was " + value + ".");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
