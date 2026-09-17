package com.timder.kontor.core.macro;

import com.timder.kontor.core.port.Rng;

public final class MacroState {

    /**
     * Days since the world started.
     */
    private long day;

    /**
     * The cycle index (1 = normal)
     */
    private double index;

    /**
     * The cycle index of the previous day.
     */
    private double previousIndex;

    /**
     * The day the current cycle began. Must be negative on world creation.
     */
    private long cycleStart;

    /**
     * Length of the current cycle.
     */
    private int cycleLength;

    /**
     * The strength of the current cycle.
     */
    private double cycleAmplitude;

    private double noise;

    private MacroState(long day, double index, long cycleStart, int cycleLength, double cycleAmplitude, double noise) {
        this.day = day;
        this.index = index;
        this.previousIndex = index;
        this.cycleStart = cycleStart;
        this.cycleLength = cycleLength;
        this.cycleAmplitude = cycleAmplitude;
        this.noise = noise;
    }

    /**
     * Creates a fresh macroeconomic cycle. Starts at a random point in the cycle.
     *
     * @param params The settings of the cycle
     * @param rng The rng source
     * @return The starting state
     */
    public static MacroState fresh(MacroParams params, Rng rng) {
        int length = rng.nextInt(params.minCycleLength(), params.maxCycleLength());
        double amplitude = rng.nextDouble(params.minAmplitude(), params.maxAmplitude());
        long start = -rng.nextInt(0, length - 1);
        return new MacroState(0, 1.0, start, length, amplitude, 0.0);
    }

    public long getDay() {
        return day;
    }

    public double getIndex() {
        return index;
    }

    public double getPreviousIndex() {
        return previousIndex;
    }

    public long getCycleStart() {
        return cycleStart;
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public double getCycleAmplitude() {
        return cycleAmplitude;
    }

    public double getNoise() {
        return noise;
    }

    /**
     * Progress of the current cycle.
     * @return Progress of the current cycle from 0 to 1
     */
    public double getCycleProgress() {
        return (double) (day - cycleStart) / cycleLength;
    }

    void setDay(long value) {
        this.day = value;
    }

    void setIndex(double value) {
        this.previousIndex = this.index;
        this.index = value;
    }

    void setNoise(double value) {
        this.noise = value;
    }

    void startNewCycle(long start, int length, double amplitude) {
        this.cycleStart = start;
        this.cycleLength = length;
        this.cycleAmplitude = amplitude;
    }

    @Override
    public String toString() {
        return "MacroState[day=%d, index=%.4f, cycle=%d/%d, amplitude=%.3f]".formatted(day, index, day - cycleStart, cycleLength, cycleAmplitude);
    }

    public record SaveState(long day, double index, double previousIndex, long cycleStart, int cycleLength, double cycleAmplitude, double noise) {}

    public SaveState getSaveState() {
        return new SaveState(day, index, previousIndex, cycleStart, cycleLength, cycleAmplitude, noise);
    }

    public static MacroState restore(SaveState saveState) {
        MacroState state = new MacroState(saveState.day(), saveState.index(), saveState.cycleStart(), saveState.cycleLength(), saveState.cycleAmplitude(), saveState.noise());
        state.previousIndex = saveState.previousIndex();
        return state;
    }
}
