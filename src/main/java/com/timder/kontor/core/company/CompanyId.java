package com.timder.kontor.core.company;

public record CompanyId(int value) {
    public CompanyId {
        if (value < 1) throw new IllegalArgumentException("value must be positive.");
    }

    @Override
    public String toString() {
        return "CompanyId[" + value + "]";
    }
}
