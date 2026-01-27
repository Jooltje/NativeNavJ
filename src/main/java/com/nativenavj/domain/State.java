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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getHeading() {
        return heading;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getRoll() {
        return roll;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getSpeed() {
        return speed;
    }

    public double getClimb() {
        return climb;
    }

    public double getTime() {
        return time;
    }

    /**
     * Creates a neutral/default state for testing.
     */
    public static State neutral() {
        return new State(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
}
