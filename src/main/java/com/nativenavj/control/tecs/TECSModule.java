package com.nativenavj.control.tecs;

import com.nativenavj.control.core.ControlFrame;
import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import com.nativenavj.control.core.LoopController;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Total Energy Control System (TECS) Module.
 * Manages Energy Balance (Throttle) and Energy Distribution (Pitch).
 */
public class TECSModule extends LoopController {
    private final AtomicReference<FlightGoal> goalRef;
    private final AtomicReference<FlightTelemetry> telemetryRef;
    private final AtomicReference<ControlFrame> controlRef;

    private static final double MIN_STALL_KTS = 40.0;
    private static final double ALT_P = 0.01; // Simple proportional gain for energy calculations
    private static final double SPD_P = 0.01;

    public TECSModule(double hz,
            AtomicReference<FlightGoal> goalRef,
            AtomicReference<FlightTelemetry> telemetryRef,
            AtomicReference<ControlFrame> controlRef) {
        super(hz);
        this.goalRef = goalRef;
        this.telemetryRef = telemetryRef;
        this.controlRef = controlRef;
    }

    @Override
    protected void step() {
        FlightGoal goal = goalRef.get();
        FlightTelemetry telemetry = telemetryRef.get();

        if (goal == null || telemetry == null || !goal.systemActive()) {
            return;
        }

        // 1. Smart Safety: Stall Protection
        if (telemetry.airspeedKts() < MIN_STALL_KTS) {
            controlRef.set(new ControlFrame(-10.0, 0.0, 1.0));
            return;
        }

        // 2. Calculations (Simplified Energy Logic)
        double altErr = goal.targetAltitudeFt() - telemetry.altitudeFt();
        double spdErr = goal.targetAirspeedKts() - telemetry.airspeedKts();

        // Energy Error (Alt_Err + Spd_Err) -> Drives Throttle
        double throttle = (altErr * ALT_P) + (spdErr * SPD_P);
        throttle = Math.max(0.0, Math.min(1.0, throttle));

        // Dist Error (Alt_Err - Spd_Err) -> Drives Pitch
        double pitch = (altErr * ALT_P) - (spdErr * SPD_P);
        pitch = Math.max(-20.0, Math.min(20.0, pitch * 10.0)); // Scale to degrees

        // Heading Error -> Drives Roll (Simplified)
        double hdgErr = goal.targetHeadingDeg() - telemetry.headingDeg();
        while (hdgErr > 180)
            hdgErr -= 360;
        while (hdgErr < -180)
            hdgErr += 360;
        double roll = Math.max(-30.0, Math.min(30.0, hdgErr));

        controlRef.set(new ControlFrame(pitch, roll, throttle));
    }
}
