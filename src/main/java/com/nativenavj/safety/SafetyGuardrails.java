package com.nativenavj.safety;

import com.nativenavj.util.LogManager;

public class SafetyGuardrails {
    // Hard limits
    private static final double MIN_ALTITUDE = 500.0;
    private static final double MAX_ALTITUDE = 45000.0;
    private static final double MIN_AIRSPEED = 65.0;

    private static final double MAX_BANK = 30.0;
    private static final double MAX_PITCH = 15.0;
    private static final double MIN_PITCH = -10.0;

    public double validateAltitude(double requestedAltitude, double currentGroundAlt) {
        double safeAlt = requestedAltitude;
        if (requestedAltitude < currentGroundAlt + MIN_ALTITUDE) {
            safeAlt = currentGroundAlt + MIN_ALTITUDE;
            LogManager.warn("Altitude guardrail! Adjusting " + requestedAltitude + " to " + safeAlt);
        }
        return Math.min(safeAlt, MAX_ALTITUDE);
    }

    public double validateHeading(double requestedHeading) {
        double validHeading = requestedHeading % 360.0;
        if (validHeading < 0)
            validHeading += 360.0;
        return validHeading;
    }

    public double validateAirspeed(double requestedAirspeed) {
        if (requestedAirspeed < MIN_AIRSPEED) {
            LogManager.warn("Airspeed guardrail! Minimum safe speed is " + MIN_AIRSPEED + "kts");
            return MIN_AIRSPEED;
        }
        return requestedAirspeed;
    }

    public double clampAileron(double aileron) {
        return Math.max(-1.0, Math.min(1.0, aileron));
    }

    public double clampElevator(double elevator) {
        return Math.max(-1.0, Math.min(1.0, elevator));
    }

    public double clampThrottle(double throttle) {
        return Math.max(0.0, Math.min(1.0, throttle));
    }

    public double protectBank(double currentBank) {
        if (Math.abs(currentBank) > MAX_BANK + 5.0) {
            LogManager
                    .error("CRITICAL: Extreme Bank angle detected (" + currentBank + ")! Emergency leveling required.");
            // This would be used to trigger an override in FlightController
        }
        return currentBank;
    }
}
