package com.nativenavj.domain;

/**
 * Aircraft telemetry state.
 * Represents the current physical state of the aircraft.
 */
public record State(
        double altitudeFt,
        double airspeedKts,
        double headingDeg,
        double pitchDeg,
        double rollDeg,
        double yawDeg,
        double verticalSpeedFpm) {
    /**
     * Creates a neutral/default state for testing.
     */
    public static State neutral() {
        return new State(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
