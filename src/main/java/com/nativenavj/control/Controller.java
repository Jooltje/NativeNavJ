package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Sample;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;

/**
 * Abstract base class for PID controllers.
 * Inherits Loop functionality and implements the discrete-time PID algorithm.
 */
public abstract class Controller extends Loop {
    protected final Memory memory;
    protected final Actuator actuator;
    protected final Configuration configuration;
    protected final Sensor sensor;

    protected double integral = 0.0;
    protected double previousFeedback = 0.0;
    protected boolean firstIteration = true;
    protected double lastSimTime = 0.0;

    /**
     * Creates a new PID controller.
     * 
     * @param memory   blackboard reference
     * @param actuator actuator reference
     * @param sensor   sensor port reference
     * @param config   configuration object
     */
    public Controller(Memory memory, Actuator actuator, Sensor sensor, Configuration config) {
        super(config.frequency());
        this.memory = memory;
        this.actuator = actuator;
        this.sensor = sensor;
        this.configuration = config;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    protected void step() {
        if (!actuator.isReady() || !configuration.active()) {
            return;
        }

        Sample sample = sensor.getSample();
        if (sample == null || sample.time() <= 0.0 || sample.time() == lastSimTime) {
            return; // No new simulation time data
        }

        Target target = memory.getTarget();
        double setpoint = getSetpoint(target);
        double feedback = sample.value();
        double error = setpoint - feedback;

        double dt = (lastSimTime == 0.0) ? (1.0 / configuration.frequency())
                : (sample.time() - lastSimTime);
        lastSimTime = sample.time();

        double output = compute(error, feedback, dt);
        sendCommand(output);
    }

    /**
     * Extracts the target setpoint for this controller.
     */
    protected abstract double getSetpoint(Target target);

    /**
     * Sends the calculated command to the actuator.
     */
    protected abstract void sendCommand(double output);

    /**
     * Computes the PID output.
     */
    public double compute(double error, double feedback, double dt) {
        // Proportional term
        double pTerm = configuration.proportional() * error;

        // Integral term with accumulation
        integral += error * dt;
        double iTerm = configuration.integral() * integral;

        // Derivative term (on feedback to avoid derivative kick)
        double derivative = 0.0;
        if (!firstIteration) {
            derivative = (feedback - previousFeedback) / dt;
        }
        double dTerm = -configuration.derivative() * derivative;

        previousFeedback = feedback;
        firstIteration = false;

        // Compute raw output
        double output = pTerm + iTerm + dTerm;

        // Clamp output to limits defined in configuration
        double clampedOutput = Math.max(configuration.min(), Math.min(configuration.max(), output));

        // Clamps the integral sum to prevent windup (anti-windup)
        if (output != clampedOutput && configuration.integral() != 0.0) {
            double excessIntegral = (output - clampedOutput) / configuration.integral();
            integral -= excessIntegral;
        }

        return clampedOutput;
    }

    /**
     * Resets the controller state.
     */
    public void reset() {
        integral = 0.0;
        previousFeedback = 0.0;
        firstIteration = true;
        lastSimTime = 0.0;
    }
}
