package com.timder.kontor.chart;

import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns market data into a chart description.
 */
public final class MarketCharts {

    private static final int COLOR_PRICE = 0xFF2A78D6;
    private static final int COLOR_COMPETITORS = 0xFFAB0F0F;
    private static final int COLOR_REFERENCE_COST = 0xFF1BAF7A;

    public static ChartSpec full(ResourceLocation market, double referenceCost, List<MarketHistoryEntry> entries) {
        int count = entries.size();
        double[] x = new double[count];
        double[] price = new double[count];
        double[] competitors = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            MarketHistoryEntry entry = entries.get(i);
            x[i] = -(double) (count - 1 - i) / MarketRules.TRADING_TICKS_PER_DAY;
            price[i] = entry.displayedPrice();
            competitors[i] = entry.companies();
            pointLabels.add("Day " + entry.day());
        }

        return ChartSpec.builder(market.toString())
                .series(new ChartSeries("Price in $", COLOR_PRICE, x, price))
                .series(new ChartSeries("Competitors", COLOR_COMPETITORS, x, competitors))
                .referenceLine("Reference cost (now)", referenceCost, COLOR_REFERENCE_COST)
                .xAxis("d", "now")
                .pointLabels(pointLabels)
                .build();
    }
}
