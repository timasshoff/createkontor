package com.timder.kontor.core.company.power;

public enum PowerKind {
    FIXED("fixed"),
    DYNAMIC("dynamic");

    private final String bookingReference;

    PowerKind(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String bookingReference() {
        return bookingReference;
    }
}
