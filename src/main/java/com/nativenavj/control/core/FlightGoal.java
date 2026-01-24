package com.nativenavj.control.core;

/**
 * Produced by: Command Parser
 * Consumed by: TECS
 */
public record FlightGoal(
                double targetAltitudeFt,
                double targetHeadingDeg,
                double targetAirspeedKts) {

        public static FlightGoal initial() {
                return new FlightGoal(Double.NaN, Double.NaN, Double.NaN);
        }
}
