package com.timder.kontor.game.ui.chart;

import com.timder.kontor.core.market.MarketHistoryEntry;
import com.timder.kontor.core.market.MarketRules;
import net.minecraft.core.registries.BuiltInRegistries;
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
            competitors[i] = entry.competitors();
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

    public static ChartSpec history(List<MarketHistoryEntry> history) {
        int count = history.size();
        double[] x = new double[count];
        double[] price = new double[count];
        double[] competitors = new double[count];
        double[] demand = new double[count];
        List<String> pointLabels = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            MarketHistoryEntry entry = history.get(i);
            x[i] = -(double) (count - 1 - i) / MarketRules.TRADING_TICKS_PER_DAY;
            price[i] = entry.displayedPrice();
            competitors[i] = entry.competitors();
            demand[i] = entry.demand();
            pointLabels.add(Component.translatable("chart.createkontor.day").getString() + entry.day());
        }

        return ChartSpec.builder(Component.translatable("chart.createkontor.market_history.title").getString())
                .series(new ChartSeries(Component.translatable("chart.createkontor.market_history.series.price").getString(), ChartColors.GREEN, x, price))
                .series(new ChartSeries(Component.translatable("chart.createkontor.market_history.series.competitors").getString(), ChartColors.RED, x, competitors))
                .series(new ChartSeries(Component.translatable("chart.createkontor.market_history.series.demand").getString(), ChartColors.BLUE, x, demand))
                .xAxis(Component.translatable("chart.createkontor.d").getString(), Component.translatable("chart.createkontor.today").getString())
                .xRangeOptions(List.of(5, 10, 30, 100, 360), 30)
                .xRangePointsPerUnit(MarketRules.TRADING_TICKS_PER_DAY)
                .pointLabels(pointLabels)
                .build();
    }
}
