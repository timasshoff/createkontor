package com.timder.kontor.core.company.legalform;

import java.util.Objects;
import java.util.Optional;

public record UpgradeApplication(int targetLevel, Phase phase, long ticksLeft, int restingDaysLeft) {

    public enum Phase {
        PROCESSING,
        RESTING
    }

    public UpgradeApplication {
        if (targetLevel < 2) throw new IllegalArgumentException("targetLevel must be at least 2.");
        Objects.requireNonNull(phase, "phase must not be null.");
        switch (phase) {
            case PROCESSING -> {
                if (ticksLeft < 1) throw new IllegalArgumentException("ticksLeft must be at least 1 while processing.");
                if (restingDaysLeft != 0) throw new IllegalArgumentException("restingDaysLeft must be 0 while processing.");
            }
            case RESTING -> {
                if (ticksLeft != 0) throw new IllegalArgumentException("ticksLeft must be 0 while resting.");
                if (restingDaysLeft < 1) throw new IllegalArgumentException("restingDaysLeft must be at least 1 while resting.");
            }
        }
    }

    public static UpgradeApplication start(LegalFormDef target) {
        Objects.requireNonNull(target, "target must not be null.");
        if (!target.hasEntryRequirements()) throw new IllegalArgumentException("Legal form " + target + " has no entry requirements.");
        return new UpgradeApplication(target.level(), Phase.PROCESSING, target.entryRequirements().processingTicks(), 0);
    }

    /**
     * A new application that is resting
     */
    public static UpgradeApplication resting(int targetLevel, int restingDays) {
        return new UpgradeApplication(targetLevel, Phase.RESTING, 0, restingDays);
    }

    public boolean isProcessing() {
        return phase == Phase.PROCESSING;
    }

    public boolean isResting() {
        return phase == Phase.RESTING;
    }

    /**
     * Lets this application process
     * @param ticks The amount of ticks to process
     * @return The application with fewer ticks left. Empty if the application is finished
     */
    public Optional<UpgradeApplication> afterTicks(long ticks) {
        if (phase != Phase.PROCESSING) throw new IllegalStateException("Only a processing application has ticks.");
        if (ticks < 1) throw new IllegalArgumentException("ticks must be at least 1.");
        if (ticks >= ticksLeft) {
            return Optional.empty();
        }
        return Optional.of(new UpgradeApplication(targetLevel, Phase.PROCESSING, ticksLeft - ticks, 0));
    }

    /**
     * Lets this application rest for one day
     * @return The application with one resting day less. Empty if there is no resting day left
     */
    public Optional<UpgradeApplication> afterRestingDay() {
        if (phase != Phase.RESTING) throw new IllegalStateException("Only a resting application has resting days.");
        if (restingDaysLeft <= 1) {
            return Optional.empty();
        }
        return Optional.of(resting(targetLevel, restingDaysLeft - 1));
    }
}
