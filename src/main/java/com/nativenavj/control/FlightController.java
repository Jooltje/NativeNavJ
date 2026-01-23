package com.nativenavj.control;

import com.nativenavj.simconnect.TelemetryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightController {
    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    private double targetHeading = 0.0;
    private double targetAltitude = 0.0;
    private double targetAirspeed = 0.0;

    private boolean headingHold = false;
    private boolean altitudeHold = false;
    private boolean airspeedHold = false;

    public void update(TelemetryData telemetry) {
        if (headingHold) {
            // In a real implementation, we would use a PID or SimConnect AP events here
            // For now, let's just log that we are maintaining state
        }

        if (altitudeHold) {
            // Same for altitude
        }
    }

    public void setTargetHeading(double heading) {
        this.targetHeading = heading;
        this.headingHold = true;
        logger.info("New target heading: {}", heading);
    }

    public void setTargetAltitude(double altitude) {
        this.targetAltitude = altitude;
        this.altitudeHold = true;
        logger.info("New target altitude: {}ft", altitude);
    }

    public void setTargetAirspeed(double airspeed) {
        this.targetAirspeed = airspeed;
        this.airspeedHold = true;
        logger.info("New target airspeed: {}kts", airspeed);
    }

    public void disableAll() {
        headingHold = false;
        altitudeHold = false;
        airspeedHold = false;
        logger.info("All flight controls disabled.");
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    public double getTargetAltitude() {
        return targetAltitude;
    }

    public double getTargetAirspeed() {
        return targetAirspeed;
    }
}
