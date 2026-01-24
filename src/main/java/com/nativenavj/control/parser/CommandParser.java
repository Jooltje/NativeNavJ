package com.nativenavj.control.parser;

import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import com.nativenavj.control.core.SystemStatus;
import com.nativenavj.util.LogManager;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Translates string commands into FlightGoal updates.
 * Pattern: Atomic Swap (Copy-on-Write)
 */
public class CommandParser {
    private final AtomicReference<FlightGoal> goalRef;
    private final AtomicReference<FlightTelemetry> telemetryRef;
    private final AtomicReference<SystemStatus> statusRef;

    public CommandParser(AtomicReference<FlightGoal> goalRef,
            AtomicReference<FlightTelemetry> telemetryRef,
            AtomicReference<SystemStatus> statusRef) {
        this.goalRef = goalRef;
        this.telemetryRef = telemetryRef;
        this.statusRef = statusRef;
    }

    public void parse(String command) {
        FlightGoal oldGoal = goalRef.get();
        if (oldGoal == null) {
            oldGoal = FlightGoal.initial(); // Changed initialization
        }

        String[] parts = command.toUpperCase().split(" ");
        if (parts.length < 1)
            return;

        try {
            switch (parts[0]) {
                case "ON":
                    FlightTelemetry tele = telemetryRef.get();
                    double targetAlt = oldGoal.targetAltitudeFt();
                    double targetHdg = oldGoal.targetHeadingDeg();
                    double targetSpd = oldGoal.targetAirspeedKts();

                    if (tele != null) {
                        if (Double.isNaN(targetAlt))
                            targetAlt = tele.altitudeFt();
                        if (Double.isNaN(targetHdg))
                            targetHdg = tele.headingDeg();
                        if (Double.isNaN(targetSpd))
                            targetSpd = tele.airspeedKts();
                    }

                    goalRef.set(new FlightGoal(targetAlt, targetHdg, targetSpd));
                    statusRef.set(new SystemStatus(true));
                    break;
                case "OFF":
                    statusRef.set(new SystemStatus(false));
                    break;
                case "ALT":
                    if (parts.length >= 2) {
                        double alt = Double.parseDouble(parts[1]);
                        goalRef.set(new FlightGoal(alt, oldGoal.targetHeadingDeg(), oldGoal.targetAirspeedKts()));
                    } else {
                        return;
                    }
                    break;
                case "HDG":
                    if (parts.length >= 2) {
                        double hdg = Double.parseDouble(parts[1]);
                        goalRef.set(new FlightGoal(oldGoal.targetAltitudeFt(), hdg, oldGoal.targetAirspeedKts()));
                    } else {
                        return;
                    }
                    break;
                case "SPD":
                    if (parts.length >= 2) {
                        double spd = Double.parseDouble(parts[1]);
                        goalRef.set(new FlightGoal(oldGoal.targetAltitudeFt(), oldGoal.targetHeadingDeg(), spd));
                    } else {
                        return;
                    }
                    break;
                default:
                    LogManager.warn("Unknown command: " + command);
                    return;
            }
            logGoal(command, statusRef.get(), goalRef.get());
        } catch (NumberFormatException e) {
            LogManager.error("Invalid command parameter in: " + command);
        }
    }

    private void logGoal(String context, SystemStatus status, FlightGoal goal) {
        LogManager.info(String.format("GOAL CHANGE [%s]: Active=%b, Alt=%.1fft, Hdg=%.1fdeg, Spd=%.1fkts",
                context, status.active(), goal.targetAltitudeFt(), goal.targetHeadingDeg(),
                goal.targetAirspeedKts()));
    }
}
