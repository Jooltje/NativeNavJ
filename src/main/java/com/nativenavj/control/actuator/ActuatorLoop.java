package com.nativenavj.control.actuator;

import com.nativenavj.control.core.ControlFrame;
import com.nativenavj.control.core.FlightGoal;
import com.nativenavj.control.core.FlightTelemetry;
import com.nativenavj.control.core.LoopController;
import com.nativenavj.control.math.GenericPID;
import com.nativenavj.simconnect.SimConnectService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * High-frequency loop for surface actuation.
 * Drives Pitch and Roll using PID controllers.
 */
public class ActuatorLoop extends LoopController {
    private final AtomicReference<FlightGoal> goalRef;
    private final AtomicReference<ControlFrame> controlRef;
    private final AtomicReference<FlightTelemetry> telemetryRef;
    private final SimConnectService simService;

    private final GenericPID pitchPID = new GenericPID(0.04, 0.01, 0.01, -1.0, 1.0);
    private final GenericPID rollPID = new GenericPID(0.02, 0.005, 0.01, -1.0, 1.0);

    private final double dt;

    public ActuatorLoop(double hz,
            AtomicReference<FlightGoal> goalRef,
            AtomicReference<ControlFrame> controlRef,
            AtomicReference<FlightTelemetry> telemetryRef,
            SimConnectService simService) {
        super(hz);
        this.goalRef = goalRef;
        this.controlRef = controlRef;
        this.telemetryRef = telemetryRef;
        this.simService = simService;
        this.dt = 1.0 / hz;
    }

    @Override
    protected void step() {
        FlightGoal goal = goalRef.get();
        ControlFrame frame = controlRef.get();
        FlightTelemetry telemetry = telemetryRef.get();

        if (goal == null || !goal.systemActive() || frame == null || telemetry == null) {
            return;
        }

        // Calculate Surface Deflections
        double elevator = pitchPID.calculate(frame.pitchTargetDeg(), telemetry.pitchDeg(), dt);
        double aileron = rollPID.calculate(frame.rollTargetDeg(), telemetry.rollDeg(), dt);
        double throttle = frame.throttlePercent(); // Throttle is set directly by TECS (20Hz is sufficient)

        // Write to Hardware (SimConnect)
        simService.actuateSurfaces(aileron, elevator, 0.0, throttle);
    }
}
