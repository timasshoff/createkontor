package com.timder.kontor.core.market;

public record MarketShareBreakdown(double own, double otherCompanies, double competition) {

    public MarketShareBreakdown {
        requireShare(own, "own");
        requireShare(otherCompanies, "otherCompanies");
        requireShare(competition, "competition");
        if (Math.abs(own + otherCompanies + competition - 1.0) > 1e-9) {
            throw new IllegalArgumentException("shares must add up to one, but are " + (own + otherCompanies + competition) + ".");
        }
    }

    private static void requireShare(double value, String name) {
        if (Double.isNaN(value) || value < 0.0 || value > 1.0 + 1e-9) {
            throw new IllegalArgumentException(name + " must be within [0, 1], but is " + value + ".");
        }
    }
}
