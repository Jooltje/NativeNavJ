package com.nativenavj.domain;

/**
 * Aircraft state per specification.
 * Represents the current physical state of the aircraft.
 */
public record State(
        double latitude,
        double longitude,
        double heading,
        double altitude,
        double roll,
        double pitch,
        double yaw,
        double speed,
        double climb,
        double time) {
    /**
     * Creates a neutral/default state for testing.
     */
    public static State neutral() {
        return new State(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
