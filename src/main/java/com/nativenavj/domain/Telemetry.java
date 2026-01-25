package com.nativenavj.domain;

/**
 * Aircraft telemetry state.
 * Represents the current physical state of the aircraft.
 */
public record Telemetry(
        double altitude,
        double speed,
        double heading,
        double pitch,
        double roll,
        double yaw,
        double rate) {
    /**
     * Creates a neutral/default telemetry for testing.
     */
    public static Telemetry neutral() {
        return new Telemetry(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
