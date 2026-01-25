package com.nativenavj.domain;

/**
 * Target flight parameters.
 * Represents the desired state the control system should achieve.
 */
public record Goal(
        double altitude,
        double speed,
        double heading) {
    /**
     * Creates a default goal for testing.
     */
    public static Goal defaultGoal() {
        return new Goal(0.0, 0.0, 0.0);
    }
}
