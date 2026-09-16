package com.timder.kontor.core.macro;

import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.port.SeededRng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MacroRulesTest {

    private static final MacroParams PARAMS = MacroParams.standard();

    private static final double BASIC_GOODS = 0.3;
    private static final double BUILDING = 1.2;
    private static final double METAL = 0.8;

    private static List<Double> runIndex(long seed, int days) {
        Rng rng = new SeededRng(seed);
        MacroState state = MacroState.fresh(PARAMS, rng);
        List<Double> index = new ArrayList<>(days);
        for (int day = 0; day < days; day++) {
            MacroRules.advanceDay(state, PARAMS, rng);
            index.add(state.getIndex());
        }
        return index;
    }

    @Test
    @DisplayName("Cycle stays within its bounds over 600 days and ten seeds")
    void staysWithinBounds() {
        for (long seed = 0; seed < 10; seed++) {
            for (double value : runIndex(seed, 600)) {
                assertTrue(value >= PARAMS.minIndex() && value <= PARAMS.maxIndex(), "index " + value + " out of bounds for seed " + seed);
            }
        }
    }

    @Test
    @DisplayName("Cycle swings noticeably but never wildly")
    void swingsAreModerate() {
        List<Double> index = runIndex(42, 600);
        double min = index.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double max = index.stream().mapToDouble(Double::doubleValue).max().orElseThrow();

        assertTrue(min < 0.90, "expected a real downswing, lowest was " + min);
        assertTrue(max > 1.10, "expected a real boom, highest was " + max);
        assertTrue(max - min < 0.50, "swing is too wild: " + (max - min));
    }

    @Test
    @DisplayName("Every cycle has a length within the configured range")
    void cycleLengthStaysInRange() {
        Rng rng = new SeededRng(3);
        MacroState state = MacroState.fresh(PARAMS, rng);

        for (int day = 0; day < 600; day++) {
            MacroRules.advanceDay(state, PARAMS, rng);
            assertTrue(state.getCycleLength() >= PARAMS.minCycleLength() && state.getCycleLength() <= PARAMS.maxCycleLength(), "cycle length " + state.getCycleLength() + " out of range");
        }
    }

    @Test
    @DisplayName("All four phases show up within 300 days")
    void allPhasesOccur() {
        Rng rng = new SeededRng(42);
        MacroState state = MacroState.fresh(PARAMS, rng);
        Set<Phase> seen = EnumSet.noneOf(Phase.class);

        for (int day = 0; day < 300; day++) {
            MacroRules.advanceDay(state, PARAMS, rng);
            seen.add(MacroRules.phase(state));
        }

        assertEquals(EnumSet.allOf(Phase.class), seen);
    }

    @Test
    @DisplayName("Two worlds with different seeds develop differently")
    void differentSeedsDiffer() {
        assertNotEquals(runIndex(1, 200), runIndex(2, 200));
    }

    @Test
    @DisplayName("No group ever falls below the floor")
    void floorHolds() {
        assertEquals(MacroRules.MIN_GROUP_FACTOR,
                MacroRules.groupFactor(0.10, 5.0), 1e-9);
    }

    @Test
    @DisplayName("Demand grows by about sixteen percent in a hundred days")
    void trendGrowsSlowly() {
        Rng rng = new SeededRng(1);
        MacroState state = MacroState.fresh(PARAMS, rng);
        for (int day = 0; day < 100; day++) {
            MacroRules.advanceDay(state, PARAMS, rng);
        }

        assertEquals(1.1617, MacroRules.trend(state, PARAMS), 0.0005);
    }
}
