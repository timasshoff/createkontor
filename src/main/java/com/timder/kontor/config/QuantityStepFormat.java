package com.timder.kontor.config;

import com.timder.kontor.core.company.request.QuantityStep;

import java.util.ArrayList;
import java.util.List;

final class QuantityStepFormat {

    static final double MIN_WEIGHT = 1.0E-6;
    static final double MAX_WEIGHT = 1.0E6;

    private record Entry(int k, double weight) {
    }

    private QuantityStepFormat() {
    }

    static boolean isValid(Object entry) {
        return parse(entry) != null;
    }

    static String format(QuantityStep step) {
        return step.k() + ":" + step.probability();
    }

    static List<QuantityStep> toSteps(List<? extends String> entries) {
        if (entries.isEmpty()) throw new IllegalArgumentException("There must be at least one quantity step.");

        List<Entry> parsed = new ArrayList<>();
        double sum = 0.0;
        for (String text : entries) {
            Entry entry = parse(text);
            if (entry == null) throw new IllegalArgumentException("Not a valid quantity step: \"" + text + "\".");
            parsed.add(entry);
            sum += entry.weight();
        }

        List<QuantityStep> steps = new ArrayList<>();
        for (Entry entry : parsed) {
            steps.add(new QuantityStep(entry.k(), entry.weight() / sum));
        }
        return steps;
    }

    private static Entry parse(Object entry) {
        if (!(entry instanceof String text)) {
            return null;
        }
        int colon = text.indexOf(':');
        if (colon < 0 || colon != text.lastIndexOf(':')) {
            return null;
        }
        try {
            int k = Integer.parseInt(text.substring(0, colon).trim());
            double weight = Double.parseDouble(text.substring(colon + 1).trim());
            if (k <= 0 || !Double.isFinite(weight) || weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
                return null;
            }
            return new Entry(k, weight);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

