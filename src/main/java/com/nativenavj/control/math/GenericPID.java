package com.nativenavj.control.math;

/**
 * Precision PID controller with clamping and anti-windup.
 */
public class GenericPID {
    private final double kp;
    private final double ki;
    private final double kd;
    private final double minOutput;
    private final double maxOutput;

    private double integral = 0;
    private double prevError = 0;
    private boolean firstRun = true;

    public GenericPID(double kp, double ki, double kd, double minOutput, double maxOutput) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.minOutput = minOutput;
        this.maxOutput = maxOutput;
    }

    /**
     * Calculates the PID output signal.
     * 
     * @param setpoint    Desired value
     * @param measurement Current value
     * @param dt          Time step in seconds
     * @return Clamped PID output
     */
    public double calculate(double setpoint, double measurement, double dt) {
        if (dt <= 0)
            return 0;

        double error = setpoint - measurement;

        // Proportional term
        double pTerm = kp * error;

        // Integral term
        integral += error * dt;
        double iTerm = ki * integral;

        // Derivative term
        double dTerm = 0;
        if (!firstRun) {
            dTerm = kd * (error - prevError) / dt;
        }
        prevError = error;
        firstRun = false;

        double output = pTerm + iTerm + dTerm;

        // Clamping (Anti-windup logic could be more sophisticated, but simple clamping
        // is a start)
        if (output > maxOutput) {
            return maxOutput;
        } else if (output < minOutput) {
            return minOutput;
        }

        return output;
    }

    public void reset() {
        this.integral = 0;
        this.prevError = 0;
        this.firstRun = true;
    }
}
