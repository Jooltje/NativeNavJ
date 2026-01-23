package com.nativenavj.safety;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafetyGuardrails {
    private static final Logger logger = LoggerFactory.getLogger(SafetyGuardrails.class);

    // Hard limits (could be aircraft specific in the future)
    private static final double MIN_ALTITUDE = 500.0; // ft AGL (simplified)
    private static final double MAX_ALTITUDE = 45000.0;
    private static final double MAX_BANK_ANGLE = 30.0; // degrees
    private static final double MIN_AIRSPEED = 65.0; // knots (Cessna 172 clean stall is roughly 50)

    public double validateAltitude(double requestedAltitude, double currentGroundAlt) {
        double safeAlt = requestedAltitude;
        if (requestedAltitude < currentGroundAlt + MIN_ALTITUDE) {
            safeAlt = currentGroundAlt + MIN_ALTITUDE;
            logger.warn("Altitude guardrail triggered! Adjusted {} to {}", requestedAltitude, safeAlt);
        }
        return Math.min(safeAlt, MAX_ALTITUDE);
    }

    public double validateHeading(double requestedHeading) {
        // Heading is cyclic 0-360
        double validHeading = requestedHeading % 360.0;
        if (validHeading < 0)
            validHeading += 360.0;
        return validHeading;
    }

    public double validateAirspeed(double requestedAirspeed) {
        if (requestedAirspeed < MIN_AIRSPEED) {
            logger.warn("Airspeed guardrail triggered! Minimum safe speed is {}kts", MIN_AIRSPEED);
            return MIN_AIRSPEED;
        }
        return requestedAirspeed;
    }
}
