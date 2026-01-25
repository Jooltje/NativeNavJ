package com.nativenavj.control;

import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;
import com.nativenavj.port.Sensor;

/**
 * Coordinator for the autonomous flight control system.
 * Manages the Computer (TECS) control loop at specified frequency.
 */
public class Coordinator extends Loop {
    private final Computer computer;

    /**
     * Creates a new coordinator.
     * 
     * @param hz       control loop frequency
     * @param sensor   telemetry input
     * @param actuator control output
     * @param clock    time source
     */
    public Coordinator(
            double hz,
            Sensor sensor,
            Actuator actuator,
            Clock clock) {
        super(hz, clock);
        this.computer = new Computer(sensor, actuator, clock);
    }

    @Override
    protected void step() {
        // Compute control commands at loop frequency
        // dt is approximately 1/hz, but Computer will track actual time
        computer.compute(1.0 / 50.0); // Assuming 50Hz default
    }

    /**
     * Gets the computer for direct access by Shell.
     */
    public Computer getComputer() {
        return computer;
    }

    /**
     * Resets the computer state.
     */
    public void reset() {
        computer.reset();
    }
}
