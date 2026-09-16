package com.timder.kontor.core.port;

public interface Rng {

    /**
     * @return A value between 0 and 1.
     */
    double nextDouble();

    /**
     * @return A normally distributed value with mean 0 and standard deviation 1.
     */
    double nextGaussian();

    default double nextDouble(double min, double max) {
        return min + nextDouble() * (max - min);
    }

    default int nextInt(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("max must not be smaller than min.");
        }
        int value = min + (int) (nextDouble() * (max - min + 1));
        return Math.min(value, max);
    }
}
