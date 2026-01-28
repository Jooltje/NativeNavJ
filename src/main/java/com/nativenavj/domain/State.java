package com.nativenavj.domain;

/**
 * Current flight state of the aircraft.
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

    public static State neutral() {
        return new State(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
