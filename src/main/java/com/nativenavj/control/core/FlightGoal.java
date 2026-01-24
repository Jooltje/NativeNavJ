package com.nativenavj.control.core;

/**
 * Produced by: Command Parser
 * Consumed by: TECS
 */
public record FlightGoal(
        boolean systemActive, // ON/OFF master switch
        double targetAltitudeFt,
        double targetHeadingDeg,
        double targetAirspeedKts) {
}
