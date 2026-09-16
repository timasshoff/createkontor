package com.timder.kontor.core.market;

public record DayResult (
        double demand,
        double soldByCompanies,
        double soldByCompetitors,
        double overflow,
        double utilisation,
        double priceBefore,
        double priceAfter,
        double companiesBefore,
        double companiesAfter
) {

    /**
     * Whether a competitor has closed.
     * @return True, if the rounded number of competitors has sunken.
     */
    public boolean competitorClosed() {
        return Math.round(companiesAfter) < Math.round(companiesBefore);
    }

    /**
     * Whether a competitor has opened.
     * @return True, if the rounded number of competitors has risen.
     */
    public boolean competitorOpened() {
        return Math.round(companiesAfter) > Math.round(companiesBefore);
    }

    /**
     * Whether competitors operate under pressure.
     * @param params The market parameters
     * @return True, if the competitors are selling at low prices and have low utilisation.
     */
    public boolean underPressure(MarketParams params) {
        return priceAfter <= params.referenceCost() * 1.01 && utilisation < 0.4;
    }
}
