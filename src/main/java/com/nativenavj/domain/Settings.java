package com.nativenavj.domain;

/**
 * Container for all controller configurations.
 * Immutable and thread-safe.
 */
public record Settings(
        Configuration roll,
        Configuration pitch,
        Configuration yaw,
        Configuration throttle) {

    public Configuration getRoll() {
        return roll;
    }

    public Configuration getPitch() {
        return pitch;
    }

    public Configuration getYaw() {
        return yaw;
    }

    public Configuration getThrottle() {
        return throttle;
    }

    /**
     * Creates default settings with standard configurations.
     */
    public static Settings defaultSettings() {
        return new Settings(
                Configuration.SURFACE,
                Configuration.SURFACE,
                Configuration.SURFACE,
                Configuration.THROTTLE);
    }
}
