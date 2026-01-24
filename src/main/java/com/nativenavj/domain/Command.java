package com.nativenavj.domain;

/**
 * Control surface commands.
 * Represents the output from the control system to the aircraft.
 */
public record Command(
        double pitchDeg,
        double rollDeg,
        double throttle,
        double rudderDeg) {
    /**
     * Creates a neutral command (level flight, idle throttle).
     */
    public static Command neutral() {
        return new Command(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Clamps all values to safe ranges.
     */
    public Command clamp() {
        return new Command(
                Math.max(-20.0, Math.min(20.0, pitchDeg)),
                Math.max(-30.0, Math.min(30.0, rollDeg)),
                Math.max(0.0, Math.min(1.0, throttle)),
                Math.max(-30.0, Math.min(30.0, rudderDeg)));
    }
}
