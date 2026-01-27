package com.nativenavj.domain;

/**
 * Target flight parameters.
 * Represents the desired state the control system should achieve.
 */
public record Goal(
        double altitude,
        double speed,
        double heading) {

    public double getAltitude() {
        return altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public double getHeading() {
        return heading;
    }

    /**
     * Creates a default goal for testing.
     */
    public static Goal defaultGoal() {
        return new Goal(0.0, 0.0, 0.0);
    }
}
