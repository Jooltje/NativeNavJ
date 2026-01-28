package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.port.Objective;
import com.nativenavj.domain.Sample;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discrete-time PID controller.
 * Implements Runnable for periodic execution.
 */
public class Controller implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    protected final Objective objective;
    protected final Actuator actuator;
    protected final Sensor sensor;
    protected final Configuration configuration;

    // PID State (Single Nouns)
    protected double sum;
    protected double feedback;
    protected double time;
    protected double error;
    protected double derivative;
    protected double output;
    protected boolean initial;

    public Controller(Objective objective, Actuator actuator, Sensor sensor, Configuration configuration) {
        this(objective, actuator, sensor, configuration, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true);
    }

    protected Controller(Objective objective, Actuator actuator, Sensor sensor, Configuration configuration,
            double sum, double feedback, double time, double error, double derivative, double output, boolean initial) {
        this.objective = objective;
        this.actuator = actuator;
        this.sensor = sensor;
        this.configuration = configuration;
        this.sum = sum;
        this.feedback = feedback;
        this.time = time;
        this.error = error;
        this.derivative = derivative;
        this.output = output;
        this.initial = initial;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Controller setConfiguration(Configuration config) {
        // Bumpless Transfer: Calculate new sum to preserve last output
        double newSum = sum;
        if (config.getIntegral() != 0) {
            newSum = (output - (config.getProportional() * error) - (config.getDerivative() * derivative))
                    / config.getIntegral();
        }

        return new Controller(objective, actuator, sensor, config, newSum, feedback, time, error, derivative, output,
                initial);
    }

    @Override
    public void run() {
        try {
            Sample sample = sensor.getSample();
            if (sample == null)
                return;

            double dt = initial ? 0.01 : sample.time() - time;
            if (dt <= 0 || dt > 1.0)
                dt = 0.01;

            double currentFeedback = sample.value();
            double currentError = objective.getTarget() - currentFeedback;

            output = compute(currentError, currentFeedback, dt, configuration);
            actuator.setSignal(output);

            this.time = sample.time();
        } catch (Exception e) {
            log.error("Error in controller execution", e);
        }
    }

    public double compute(double currentError, double currentFeedback, double dt, Configuration config) {
        double pTerm = config.getProportional() * currentError;

        sum += currentError * dt;
        double iTerm = config.getIntegral() * sum;

        derivative = 0.0;
        if (!initial) {
            derivative = (currentFeedback - feedback) / dt;
        }
        double dTerm = -config.getDerivative() * derivative;

        double out = pTerm + iTerm + dTerm;
        double clamped = Math.max(config.getMin(), Math.min(config.getMax(), out));

        // Anti-windup
        if (out != clamped && config.getIntegral() != 0.0) {
            double excess = (out - clamped) / config.getIntegral();
            sum -= excess;
        }

        // Update state
        this.feedback = currentFeedback;
        this.error = currentError;
        this.initial = false;

        return clamped;
    }

    public void reset() {
        sum = 0.0;
        feedback = 0.0;
        time = 0.0;
        error = 0.0;
        derivative = 0.0;
        output = 0.0;
        initial = true;
    }
}
