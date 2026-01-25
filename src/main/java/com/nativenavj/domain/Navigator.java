package com.nativenavj.domain;

/**
 * Control system status.
 * Represents whether the autonomous control system is active.
 */
public record Navigator(
        boolean active,
        String mode) {
    /**
     * Creates an inactive status.
     */
    public static Navigator inactive() {
        return new Navigator(false, "STANDBY");
    }

    /**
     * Creates an active status with the given mode.
     */
    public static Navigator active(String mode) {
        return new Navigator(true, mode);
    }
}
