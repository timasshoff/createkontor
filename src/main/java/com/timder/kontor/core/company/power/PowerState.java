package com.timder.kontor.core.company.power;

/**
 * The stress units a company has bought but not yet paid for
 * @param fixedSuTicks The fixed unbilled SU ticks
 * @param dynamicSuTicks The dynamic unbilled SU ticks
 */
public record PowerState(long fixedSuTicks, long dynamicSuTicks) {

    private static final PowerState EMPTY = new PowerState(0, 0);

    public PowerState {
        if (fixedSuTicks < 0) throw new IllegalArgumentException("fixedSuTicks must not be negative.");
        if (dynamicSuTicks < 0) throw new IllegalArgumentException("dynamicSuTicks must not be negative.");
    }

    public static PowerState empty() {
        return EMPTY;
    }

    public long suTicks(PowerKind kind) {
        return switch (kind) {
            case FIXED -> fixedSuTicks;
            case DYNAMIC -> dynamicSuTicks;
        };
    }

    public boolean isEmpty() {
        return fixedSuTicks == 0 && dynamicSuTicks == 0;
    }

    public PowerState with(PowerKind kind, long suTicks) {
        if (suTicks < 0) throw new IllegalArgumentException("suTicks must not be negative.");
        return switch (kind) {
            case FIXED -> new PowerState(add(fixedSuTicks, suTicks), dynamicSuTicks);
            case DYNAMIC -> new PowerState(fixedSuTicks, add(dynamicSuTicks, suTicks));
        };
    }

    public PowerState without(PowerKind kind) {
        return switch (kind) {
            case FIXED -> new PowerState(0, dynamicSuTicks);
            case DYNAMIC -> new PowerState(fixedSuTicks, 0);
        };
    }

    private static long add(long current, long added) {
        long room = Long.MAX_VALUE - current;
        return added > room ? Long.MAX_VALUE : current + added;
    }
}
