package com.nativenavj.domain;

/**
 * Target flight parameters.
 * Represents the desired state the control system should achieve.
 */
public record Goal(
        double targetAltitudeFt,
        double targetAirspeedKts,
        double targetHeadingDeg) {
    /**
     * Creates a default goal for testing.
     */
    public static Goal defaultGoal() {
        return new Goal(5000.0, 120.0, 0.0);
    }
}
