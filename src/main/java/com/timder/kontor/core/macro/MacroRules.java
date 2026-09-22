package com.timder.kontor.core.macro;

import com.timder.kontor.core.port.Rng;

public class MacroRules {

    public static final double BOOM_THRESHOLD = 1.05;
    public static final double RECESSION_THRESHOLD = 0.95;
    public static final double MIN_GROUP_FACTOR = 0.3;

    /**
     * Comparison grid for rates
     */
    private static final double RATE_GRID = 1_000_000.0;

    public static void advanceDay(MacroState state, MacroParams params, Rng rng) {
        state.setDay(state.getDay() + 1);

        if (state.getDay() - state.getCycleStart() >= state.getCycleLength()) {
            state.startNewCycle(
                    state.getDay(),
                    rng.nextInt(params.minCycleLength(), params.maxCycleLength()),
                    rng.nextDouble(params.minAmplitude(), params.maxAmplitude())
            );
        }

        state.setNoise(params.noiseDecay() * state.getNoise() + params.noiseScale() * rng.nextGaussian());
        double wave = state.getCycleAmplitude() * Math.sin(2.0 * Math.PI * state.getCycleProgress());
        state.setIndex(clamp(1.0 + wave + state.getNoise(), params.minIndex(), params.maxIndex()));

        if (isRateDecisionDay(state.getDay(), params.policyRateParams())) {
            state.setPolicyRate(nextPolicyRate(state.getPolicyRate(), state.getIndex(), params.policyRateParams()));
        }
    }

    /**
     * How strongly a cycle hits a product group.
     * @param state The state of the cycle
     * @param cycleSensitivity The sensitivity of the product group
     * @return The demand factor (1 = normal)
     */
    public static double groupFactor(MacroState state, double cycleSensitivity) {
        return groupFactor(state.getIndex(), cycleSensitivity);
    }

    public static double groupFactor(double index, double cycleSensitivity) {
        return Math.max(MIN_GROUP_FACTOR, 1.0 + cycleSensitivity * (index - 1.0));
    }

    /**
     * Long term grow of the economy.
     * @param state The state of the cycle
     * @param params The parameters of the cycle
     * @return A growing factor representing the growth
     */
    public static double trend(MacroState state, MacroParams params) {
        return Math.pow(1.0 + params.trendPerDay(), state.getDay());
    }

    /**
     * The demand of one product on this day.
     * @param baseDemand The base demand of the product
     * @param state The state of the cycle
     * @param params The parameters of the cycle
     * @param cycleSensitivity The sensitivity of the product group
     * @return The demand for the product
     */
    public static double demand(double baseDemand, MacroState state, MacroParams params, double cycleSensitivity) {
        return baseDemand * groupFactor(state, cycleSensitivity) * trend(state, params);
    }

    public static Phase phase(MacroState state) {
        boolean rising = state.getIndex() > state.getPreviousIndex();
        if (rising) {
            return state.getIndex() >= BOOM_THRESHOLD ? Phase.BOOM : Phase.UPSWING;
        }
        return state.getIndex() >= RECESSION_THRESHOLD ? Phase.DOWNSWING : Phase.RECESSION;
    }

    /**
     * How much cheaper processing has become since the world started.
     * @param state The state of the cycle
     * @param params The parameters of technical progress
     * @return A factor representing the technical progress starting at one and slowly falling towards the floor.
     */
    public static double technicalProgress(MacroState state, ProgressParams params) {
        return Math.max(params.floor(), Math.pow(1.0 - params.dailyDecay(), state.getDay()));
    }

    /**
     * Whether the policy rate can change on this day
     * @param day The current day
     * @param params The policy rate parameters
     * @return True on every day that is a multiple of the decision interval
     */
    public static boolean isRateDecisionDay(long day, PolicyRateParams params) {
        return day > 0 && day % params.decisionIntervalDays() == 0;
    }

    /**
     * The rate that the policy rate aims for.
     * @param index The current cycle index
     * @param params The policy rate parameters
     * @return The target rate as a fraction
     */
    public static double targetPolicyRate(double index, PolicyRateParams params) {
        double target = params.baseRate() * (1.0 + params.targetSensitivity() * (index - 1.0));
        return clamp(target, params.minRate(), params.maxRate());
    }

    /**
     * Computes the next policy rate decision
     * @param current The current rate as a fraction
     * @param index The current cycle index
     * @param params The policy rate parameters
     * @return The rate after the decision
     */
    public static double nextPolicyRate(double current, double index, PolicyRateParams params) {
        long currentMicro = toMicro(current);
        long distance = toMicro(targetPolicyRate(index, params)) - currentMicro;
        long distanceAbs = Math.abs(distance);

        long step;
        if (distanceAbs > toMicro(params.largeThreshold())) {
            step = toMicro(params.largeStep());
        } else if (distanceAbs > toMicro(params.smallThreshold())) {
            step = toMicro(params.smallStep());
        } else {
            return current;
        }

        long next = currentMicro + Long.signum(distance) * step;
        next = Math.max(toMicro(params.minRate()), Math.min(toMicro(params.maxRate()), next));
        return next / RATE_GRID;
    }

    private static long toMicro(double rate) {
        return Math.round(rate * RATE_GRID);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
