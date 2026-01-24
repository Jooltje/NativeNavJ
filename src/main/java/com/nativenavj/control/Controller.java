package com.nativenavj.control;

import com.nativenavj.port.Clock;

/**
 * Abstract base class for PID controllers.
 * Implements the discrete-time PID algorithm with anti-windup.
 */
public abstract class Controller {
    protected final double kp; // Proportional gain
    protected final double ki; // Integral gain
    protected final double kd; // Derivative gain
    protected final Clock clock;

    protected double integral = 0.0;
    protected double previousFeedback = 0.0;
    protected boolean firstIteration = true;

    protected double outputMin = Double.NEGATIVE_INFINITY;
    protected double outputMax = Double.POSITIVE_INFINITY;

    /**
     * Creates a new PID controller.
     * 
     * @param kp    proportional gain
     * @param ki    integral gain
     * @param kd    derivative gain
     * @param clock time source for deterministic testing
     */
    public Controller(double kp, double ki, double kd, Clock clock) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.clock = clock;
    }

    /**
     * Sets output limits for clamping and anti-windup.
     * 
     * @param min minimum output value
     * @param max maximum output value
     */
    public void setOutputLimits(double min, double max) {
        this.outputMin = min;
        this.outputMax = max;
    }

    /**
     * Computes the PID output.
     * 
     * @param error    the error signal (setpoint - feedback)
     * @param feedback the current process variable
     * @param dt       time delta since last computation
     * @return control output
     */
    public double compute(double error, double feedback, double dt) {
        // Proportional term
        double pTerm = kp * error;

        // Integral term with accumulation
        integral += error * dt;
        double iTerm = ki * integral;

        // Derivative term (on feedback to avoid derivative kick)
        double derivative = 0.0;
        if (!firstIteration) {
            derivative = (feedback - previousFeedback) / dt;
        }
        double dTerm = -kd * derivative; // Negative because we want to oppose changes

        previousFeedback = feedback;
        firstIteration = false;

        // Compute raw output
        double output = pTerm + iTerm + dTerm;

        // Clamp output
        double clampedOutput = Math.max(outputMin, Math.min(outputMax, output));

        // Anti-windup: back-calculate integral if output is saturated
        if (output != clampedOutput && ki != 0.0) {
            // Remove the excess from integral
            double excessIntegral = (output - clampedOutput) / ki;
            integral -= excessIntegral;
        }

        return clampedOutput;
    }

    /**
     * Resets the controller state.
     * Clears integral accumulator and derivative history.
     */
    public void reset() {
        integral = 0.0;
        previousFeedback = 0.0;
        firstIteration = true;
    }
}
