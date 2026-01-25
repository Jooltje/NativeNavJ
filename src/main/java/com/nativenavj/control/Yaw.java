package com.nativenavj.control;

import com.nativenavj.port.Clock;

/**
 * Yaw controller for rudder control.
 * Manages coordinated turns and yaw damping.
 */
public class Yaw extends Controller {
    private static final double DEFAULT_KP = 0.3;
    private static final double DEFAULT_KI = 0.01;
    private static final double DEFAULT_KD = 0.05;

    private static final double MIN_RUDDER_DEG = -30.0;
    private static final double MAX_RUDDER_DEG = 30.0;

    public Yaw(Clock clock) {
        super(DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        setOutputLimits(MIN_RUDDER_DEG, MAX_RUDDER_DEG);
    }

    public Yaw(double kp, double ki, double kd, Clock clock) {
        super(kp, ki, kd, clock);
        setOutputLimits(MIN_RUDDER_DEG, MAX_RUDDER_DEG);
    }

    /**
     * Computes rudder command for coordinated turn.
     * 
     * @param rollDeg          current roll angle (for coordination)
     * @param yawRateDegPerSec current yaw rate
     * @param dt               time delta
     * @return rudder command in degrees
     */
    public double compute(double rollDeg, double yawRateDegPerSec, double dt) {
        // Simple coordination: rudder opposes yaw rate
        // More sophisticated: coordinate with roll angle
        double error = -yawRateDegPerSec; // Dampen yaw rate
        return compute(error, yawRateDegPerSec, dt);
    }
}
