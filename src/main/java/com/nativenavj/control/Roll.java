package com.nativenavj.control;

import com.nativenavj.port.Clock;

/**
 * Roll controller for aileron control.
 * Manages aircraft roll attitude to achieve target bank angles.
 */
public class Roll extends Controller {
    private static final double DEFAULT_KP = 0.8;
    private static final double DEFAULT_KI = 0.02;
    private static final double DEFAULT_KD = 0.15;

    private static final double MIN_ROLL_DEG = -30.0;
    private static final double MAX_ROLL_DEG = 30.0;

    public Roll(Clock clock) {
        super(DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        setOutputLimits(MIN_ROLL_DEG, MAX_ROLL_DEG);
    }

    public Roll(double kp, double ki, double kd, Clock clock) {
        super(kp, ki, kd, clock);
        setOutputLimits(MIN_ROLL_DEG, MAX_ROLL_DEG);
    }

    /**
     * Computes roll command based on heading error.
     * 
     * @param targetHeadingDeg  desired heading
     * @param currentHeadingDeg current heading
     * @param dt                time delta
     * @return roll command in degrees
     */
    public double compute(double targetHeadingDeg, double currentHeadingDeg, double dt) {
        // Normalize heading error to [-180, 180]
        double error = targetHeadingDeg - currentHeadingDeg;
        while (error > 180.0)
            error -= 360.0;
        while (error < -180.0)
            error += 360.0;

        return super.compute(error, currentHeadingDeg, dt);
    }
}
