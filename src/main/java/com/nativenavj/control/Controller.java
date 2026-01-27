package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.port.Objective;
import com.nativenavj.domain.Sample;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;

/**
 * Abstract base class for PID controllers.
 * Implements the Runnable interface for periodic execution and the
 * discrete-time PID algorithm.
 */
public class Controller implements Runnable {
    protected final Objective objective;
    protected final Actuator actuator;
    protected final Configuration configuration;
    protected final Sensor sensor;

    protected double sum = 0.0;
    protected double previous = 0.0;
    protected boolean firstIteration = true;
    protected double lastSimTime = 0.0;

    /**
     * Creates a new PID controller.
     * 
     * @param objective objective port reference
     * @param actuator  actuator port reference
     * @param sensor    sensor port reference
     * @param config    configuration object
     */
    public Controller(Objective objective, Actuator actuator, Sensor sensor, Configuration config) {
        this.objective = objective;
        this.actuator = actuator;
        this.sensor = sensor;
        this.configuration = config;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void run() {
        if (!configuration.active()) {
            return;
        }

        Sample sample = sensor.getSample();
        if (sample == null || sample.time() <= 0.0 || sample.time() == lastSimTime) {
            return; // No new simulation time data
        }

        double setpoint = objective.getTarget();
        double feedback = sample.value();
        double error = setpoint - feedback;

        double dt = (lastSimTime == 0.0) ? (1.0 / configuration.frequency())
                : (sample.time() - lastSimTime);
        lastSimTime = sample.time();

        double output = compute(error, feedback, dt);
        actuator.setSignal(output);
    }

    /**
     * Computes the PID output.
     */
    public double compute(double error, double feedback, double dt) {
        // Proportional term
        double pTerm = configuration.proportional() * error;

        // Integral term with accumulation
        sum += error * dt;
        double iTerm = configuration.integral() * sum;

        // Derivative term (on feedback to avoid derivative kick)
        double derivative = 0.0;
        if (!firstIteration) {
            derivative = (feedback - previous) / dt;
        }
        double dTerm = -configuration.derivative() * derivative;

        previous = feedback;
        firstIteration = false;

        // Compute raw output
        double output = pTerm + iTerm + dTerm;

        // Clamp output to limits defined in configuration
        double clampedOutput = Math.max(configuration.min(), Math.min(configuration.max(), output));

        // Clamps the integral sum to prevent windup (anti-windup)
        if (output != clampedOutput && configuration.integral() != 0.0) {
            double excessSum = (output - clampedOutput) / configuration.integral();
            sum -= excessSum;
        }

        return clampedOutput;
    }

    /**
     * Resets the controller state.
     */
    public void reset() {
        sum = 0.0;
        previous = 0.0;
        firstIteration = true;
        lastSimTime = 0.0;
    }
}
