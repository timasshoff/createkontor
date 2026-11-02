package com.timder.kontor.core.company.property.measurement;

public record MeasurementGrid(int centerX, int centerZ, int radius, int step) {

    public static final int MAX_POINTS_PER_SIDE = 1999;

    public MeasurementGrid {
        if (radius < 0) throw new IllegalArgumentException("radius must not be negative.");
        if (step < 1) throw new IllegalArgumentException("step must be at least 1.");
        int stepsPerDirection = radius / step;
        long side = 2L * stepsPerDirection + 1L;
        if (side > MAX_POINTS_PER_SIDE) {
            throw new IllegalArgumentException("The grid would have " + side + " points per side, but at most "
                    + MAX_POINTS_PER_SIDE + " are allowed. Use a larger step or a smaller radius.");
        }
        long reach = (long) stepsPerDirection * step;
        if (centerX - reach < Integer.MIN_VALUE
                || centerX + reach > Integer.MAX_VALUE
                || centerZ - reach < Integer.MIN_VALUE
                || centerZ + reach > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The grid reaches outside the range of block coordinates.");
        }
    }

    public int stepsPerDirection() {
        return radius / step;
    }

    public int pointsPerSide() {
        return 2 * stepsPerDirection() + 1;
    }

    public long pointCount() {
        long side = pointsPerSide();
        return side * side;
    }

    public int xAt(long index) {
        requireIndex(index);
        long column = index % pointsPerSide();
        return (int) (centerX + (column - stepsPerDirection()) * step);
    }

    public int zAt(long index) {
        requireIndex(index);
        long row = index / pointsPerSide();
        return (int) (centerZ + (row - stepsPerDirection()) * step);
    }

    private void requireIndex(long index) {
        if (index < 0 || index >= pointCount()) {
            throw new IllegalArgumentException("index must be between 0 and " + (pointCount() - 1) + ".");
        }
    }
}
