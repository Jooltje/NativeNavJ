package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;

/**
 * Abstract base class for PID controllers.
 * Inherits Loop functionality and implements the discrete-time PID algorithm.
 */
public abstract class Controller extends Loop {
    protected final Memory memory;
    protected final Actuator actuator;
    protected final double kp; // Proportional gain
    protected final double ki; // Integral gain
    protected final double kd; // Derivative gain

    protected double integral = 0.0;
    protected double previousFeedback = 0.0;
    protected boolean firstIteration = true;
    protected long lastStepNanos = 0;

    protected double outputMin = Double.NEGATIVE_INFINITY;
    protected double outputMax = Double.POSITIVE_INFINITY;

    /**
     * Creates a new PID controller.
     * 
     * @param hz       frequency in hertz
     * @param memory   blackboard reference
     * @param actuator actuator reference
     * @param kp       proportional gain
     * @param ki       integral gain
     * @param kd       derivative gain
     * @param clock    time source
     */
    public Controller(double hz, Memory memory, Actuator actuator, double kp, double ki, double kd, Clock clock) {
        super(hz, clock);
        this.memory = memory;
        this.actuator = actuator;
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    /**
     * Sets output limits for clamping and anti-windup.
     */
    public void setOutputLimits(double min, double max) {
        this.outputMin = min;
        this.outputMax = max;
    }

    @Override
    protected void step() {
        if (!actuator.isReady()) {
            return;
        }

        Target target = memory.getTarget();
        State state = memory.getState();

        double setpoint = getSetpoint(target);
        double feedback = getFeedback(state);
        double error = setpoint - feedback;

        long now = clock.nanoTime();
        double dt = (lastStepNanos == 0) ? (1.0 / (1_000_000_000.0 / periodNanos))
                : (now - lastStepNanos) / 1_000_000_000.0;
        lastStepNanos = now;

        double output = compute(error, feedback, dt);
        sendCommand(output);
    }

    /**
     * Extracts the target setpoint for this controller.
     */
    protected abstract double getSetpoint(Target target);

    /**
     * Extracts the current feedback for this controller.
     */
    protected abstract double getFeedback(State state);

    /**
     * Sends the calculated command to the actuator.
     */
    protected abstract void sendCommand(double output);

    /**
     * Computes the PID output.
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
        double dTerm = -kd * derivative;

        previousFeedback = feedback;
        firstIteration = false;

        // Compute raw output
        double output = pTerm + iTerm + dTerm;

        // Clamp output
        double clampedOutput = Math.max(outputMin, Math.min(outputMax, output));

        // Anti-windup: back-calculate integral if output is saturated
        if (output != clampedOutput && ki != 0.0) {
            double excessIntegral = (output - clampedOutput) / ki;
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
        lastStepNanos = 0;
    }
}
