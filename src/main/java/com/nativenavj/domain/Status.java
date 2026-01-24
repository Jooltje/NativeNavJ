package com.nativenavj.domain;

/**
 * System operational status.
 * Represents whether the autonomous control system is active.
 */
public record Status(
        boolean active,
        String mode) {
    /**
     * Creates an inactive status.
     */
    public static Status inactive() {
        return new Status(false, "STANDBY");
    }

    /**
     * Creates an active status with the given mode.
     */
    public static Status active(String mode) {
        return new Status(true, mode);
    }
}
