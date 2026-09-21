package com.timder.kontor.game.network;

import com.timder.kontor.chart.ChartReferenceLine;
import com.timder.kontor.chart.ChartSeries;
import com.timder.kontor.chart.ChartSpec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

final class ChartSpecCodec {

    private ChartSpecCodec() {
    }

    static void write(FriendlyByteBuf buf, ChartSpec spec) {
        buf.writeUtf(spec.title(), ChartSpec.MAX_TITLE_LENGTH);
        buf.writeBoolean(spec.includeZero());
        buf.writeUtf(spec.xUnit(), ChartSpec.MAX_UNIT_LENGTH);
        buf.writeUtf(spec.xZeroLabel(), ChartSpec.MAX_UNIT_LENGTH);
        buf.writeUtf(spec.yUnit(), ChartSpec.MAX_UNIT_LENGTH);
        buf.writeVarInt(spec.tooltipDecimals());

        buf.writeVarInt(spec.series().size());
        for (ChartSeries series : spec.series()) {
            buf.writeUtf(series.label(), ChartSpec.MAX_LABEL_LENGTH);
            buf.writeInt(series.color());
            buf.writeVarInt(series.size());
            for (int i = 0; i < series.size(); i++) {
                buf.writeDouble(series.x(i));
            }
            for (int i = 0; i < series.size(); i++) {
                buf.writeDouble(series.y(i));
            }
        }

        buf.writeVarInt(spec.referenceLines().size());
        for (ChartReferenceLine line : spec.referenceLines()) {
            buf.writeUtf(line.label(), ChartSpec.MAX_LABEL_LENGTH);
            buf.writeDouble(line.value());
            buf.writeInt(line.color());
        }

        buf.writeVarInt(spec.pointLabels().size());
        for (String label : spec.pointLabels()) {
            buf.writeUtf(label, ChartSpec.MAX_POINT_LABEL_LENGTH);
        }
    }

    static ChartSpec read(FriendlyByteBuf buf) {
        String title = buf.readUtf(ChartSpec.MAX_TITLE_LENGTH);
        boolean includeZero = buf.readBoolean();
        String xUnit = buf.readUtf(ChartSpec.MAX_UNIT_LENGTH);
        String xZeroLabel = buf.readUtf(ChartSpec.MAX_UNIT_LENGTH);
        String yUnit = buf.readUtf(ChartSpec.MAX_UNIT_LENGTH);
        int tooltipDecimals = buf.readVarInt();

        int seriesCount = readCount(buf, ChartSpec.MAX_SERIES, "series");
        List<ChartSeries> series = new ArrayList<>(seriesCount);
        for (int s = 0; s < seriesCount; s++) {
            String label = buf.readUtf(ChartSpec.MAX_LABEL_LENGTH);
            int color = buf.readInt();
            int size = readCount(buf, ChartSpec.MAX_POINTS, "points");
            double[] x = new double[size];
            double[] y = new double[size];
            for (int i = 0; i < size; i++) {
                x[i] = buf.readDouble();
            }
            for (int i = 0; i < size; i++) {
                y[i] = buf.readDouble();
            }
            series.add(new ChartSeries(label, color, x, y));
        }

        int lineCount = readCount(buf, ChartSpec.MAX_REFERENCE_LINES, "reference lines");
        List<ChartReferenceLine> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            lines.add(new ChartReferenceLine(buf.readUtf(ChartSpec.MAX_LABEL_LENGTH), buf.readDouble(), buf.readInt()));
        }

        int labelCount = readCount(buf, ChartSpec.MAX_POINTS, "point labels");
        List<String> pointLabels = new ArrayList<>(labelCount);
        for (int i = 0; i < labelCount; i++) {
            pointLabels.add(buf.readUtf(ChartSpec.MAX_POINT_LABEL_LENGTH));
        }

        return new ChartSpec(title, series, lines, includeZero, xUnit, xZeroLabel, yUnit, tooltipDecimals, pointLabels);
    }

    private static int readCount(FriendlyByteBuf buf, int max, String what) {
        int count = buf.readVarInt();
        if (count < 0 || count > max) {
            throw new IllegalArgumentException("too many " + what + ": " + count + " (max " + max + ")");
        }
        return count;
    }
}
