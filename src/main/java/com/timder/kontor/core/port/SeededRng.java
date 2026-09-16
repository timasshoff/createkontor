package com.timder.kontor.core.port;

import java.util.Random;

/**
 * A reproducible {@link Rng}.
 */
public class SeededRng implements Rng {

    private final Random random;

    public SeededRng(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Creates a generator from a base seed and a name.
     * Seed is built from the base seed and the hash value of the name.
     * @param seed The base seed (e.g. world seed)
     * @param name Anything that identifies this generator.
     * @return The generator
     */
    public static SeededRng forName(long seed, String name) {
        return new SeededRng(seed * 31L + name.hashCode());
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return random.nextGaussian();
    }
}
