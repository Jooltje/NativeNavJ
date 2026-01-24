package com.nativenavj.control;

import com.nativenavj.domain.Goal;
import com.nativenavj.domain.Status;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;
import com.nativenavj.port.Sensor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Coordinator for the autonomous flight control system.
 * Manages the Computer (TECS) control loop at specified frequency.
 */
public class Coordinator extends Loop {
    private final Computer computer;
    private final AtomicReference<Goal> goalRef;
    private final AtomicReference<Status> statusRef;

    /**
     * Creates a new coordinator.
     * 
     * @param hz        control loop frequency
     * @param sensor    telemetry input
     * @param actuator  control output
     * @param clock     time source
     * @param goalRef   shared goal reference
     * @param statusRef shared status reference
     */
    public Coordinator(
            double hz,
            Sensor sensor,
            Actuator actuator,
            Clock clock,
            AtomicReference<Goal> goalRef,
            AtomicReference<Status> statusRef) {
        super(hz, clock);
        this.computer = new Computer(sensor, actuator, clock);
        this.goalRef = goalRef;
        this.statusRef = statusRef;
    }

    @Override
    protected void step() {
        Goal goal = goalRef.get();
        Status status = statusRef.get();

        if (goal == null || status == null) {
            return;
        }

        // Compute control commands at loop frequency
        // dt is approximately 1/hz, but Computer will track actual time
        computer.compute(goal, status, 1.0 / 50.0); // Assuming 50Hz default
    }

    /**
     * Resets the computer state.
     */
    public void reset() {
        computer.reset();
    }
}
