package com.nativenavj.domain;

/**
 * Target flight parameters.
 * Represents the desired state the control system should achieve.
 */
public record Goal(
        double height,
        double velocity,
        double direction) {

    public static final Goal DEFAULT = new Goal(0.0, 0.0, 0.0);

    public static Goal defaultGoal() {
        return DEFAULT;
    }
}
