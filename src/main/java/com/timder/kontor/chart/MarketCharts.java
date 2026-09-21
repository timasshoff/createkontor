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
                .series(new ChartSeries("Price in $", ChartColors.BLUE, x, price))
                .series(new ChartSeries("Competitors", ChartColors.RED, x, competitors))
                .referenceLine("Reference cost (now)", referenceCost, ChartColors.TEAL)
                .xAxis("d", "now")
                .pointLabels(pointLabels)
                .build();
    }
}
