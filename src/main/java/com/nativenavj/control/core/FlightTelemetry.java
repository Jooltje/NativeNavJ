package com.nativenavj.control.core;

/**
 * Produced by: Sensor Thread
 * Consumed by: TECS, PIDs
 */
public record FlightTelemetry(
        double altitudeFt,
        double airspeedKts,
        double pitchDeg,
        double rollDeg,
        double headingDeg,
        double verticalSpeedFpm,
        long timestampNs) {
}
