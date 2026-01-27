package com.nativenavj.domain;

/**
 * Configuration for a controller.
 * Immutable and thread-safe.
 */
public record Configuration(
        boolean active,
        double frequency,
        double proportional,
        double integral,
        double derivative,
        double min,
        double max) {

    /**
     * Default configuration for a guidance controller.
     */
    public static final Configuration GUIDANCE = new Configuration(true, 10.0, 1.0, 0.0, 0.0, -1.0, 1.0);

    /**
     * Default configuration for a surface controller.
     */
    public static final Configuration SURFACE = new Configuration(true, 20.0, 0.1, 0.0, 0.0, -1.0, 1.0);

    /**
     * Default configuration for a throttle controller.
     */
    public static final Configuration THROTTLE = new Configuration(true, 5.0, 0.05, 0.01, 0.0, 0.0, 1.0);
}
