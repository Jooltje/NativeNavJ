package com.nativenavj.domain;

/**
 * PID Configuration for a specific control loop.
 */
public record Configuration(
        double proportion,
        double integral,
        double derivative,
        double minimum,
        double maximum) {

    public static final Configuration SURFACE = new Configuration(1.0, 0.1, 0.05, -1.0, 1.0);
    public static final Configuration THROTTLE_CONTROL = new Configuration(0.5, 0.05, 0.01, 0.0, 1.0);
    public static final Configuration SPEED_CONTROL = new Configuration(1.0, 0.0, 0.0, -20.0, 20.0);
    public static final Configuration ALTITUDE_CONTROL = new Configuration(0.1, 0.0, 0.0, -1000.0, 1000.0);
    public static final Configuration HEADING_CONTROL = new Configuration(2.0, 0.0, 0.0, -30.0, 30.0);
    public static final Configuration CLIMB = new Configuration(0.5, 0.05, 0.01, 0.0, 1.0);
}
