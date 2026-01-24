package com.nativenavj.control.parser;

import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.util.LogManager;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Translates string commands into FlightGoal updates.
 * Pattern: Atomic Swap (Copy-on-Write)
 */
public class CommandParser {
    private final AtomicReference<FlightGoal> goalRef;

    public CommandParser(AtomicReference<FlightGoal> goalRef) {
        this.goalRef = goalRef;
    }

    public void parse(String command) {
        FlightGoal oldGoal = goalRef.get();
        if (oldGoal == null) {
            oldGoal = new FlightGoal(false, 0, 0, 0);
        }

        String[] parts = command.toUpperCase().split(" ");
        if (parts.length < 1)
            return;

        try {
            switch (parts[0]) {
                case "ON":
                    goalRef.set(new FlightGoal(true, oldGoal.targetAltitudeFt(), oldGoal.targetHeadingDeg(),
                            oldGoal.targetAirspeedKts()));
                    LogManager.info("FCS Master Switch: ON");
                    break;
                case "OFF":
                    goalRef.set(new FlightGoal(false, oldGoal.targetAltitudeFt(), oldGoal.targetHeadingDeg(),
                            oldGoal.targetAirspeedKts()));
                    LogManager.info("FCS Master Switch: OFF");
                    break;
                case "ALT":
                    if (parts.length >= 2) {
                        double alt = Double.parseDouble(parts[1]);
                        goalRef.set(new FlightGoal(oldGoal.systemActive(), alt, oldGoal.targetHeadingDeg(),
                                oldGoal.targetAirspeedKts()));
                        LogManager.info("Target Altitude set to: " + alt);
                    }
                    break;
                case "HDG":
                    if (parts.length >= 2) {
                        double hdg = Double.parseDouble(parts[1]);
                        goalRef.set(new FlightGoal(oldGoal.systemActive(), oldGoal.targetAltitudeFt(), hdg,
                                oldGoal.targetAirspeedKts()));
                        LogManager.info("Target Heading set to: " + hdg);
                    }
                    break;
                case "SPD":
                    if (parts.length >= 2) {
                        double spd = Double.parseDouble(parts[1]);
                        goalRef.set(new FlightGoal(oldGoal.systemActive(), oldGoal.targetAltitudeFt(),
                                oldGoal.targetHeadingDeg(), spd));
                        LogManager.info("Target Airspeed set to: " + spd);
                    }
                    break;
                default:
                    LogManager.warn("Unknown command: " + command);
            }
        } catch (NumberFormatException e) {
            LogManager.error("Invalid command parameter in: " + command);
        }
    }
}
