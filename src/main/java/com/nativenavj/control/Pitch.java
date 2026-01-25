package com.nativenavj.control;

import com.nativenavj.port.Clock;

/**
 * Pitch controller for elevator control.
 * Manages aircraft pitch attitude to achieve target pitch angles.
 */
public class Pitch extends Controller {
    private static final double DEFAULT_KP = 0.5;
    private static final double DEFAULT_KI = 0.05;
    private static final double DEFAULT_KD = 0.1;

    private static final double MIN_PITCH_DEG = -20.0;
    private static final double MAX_PITCH_DEG = 20.0;

    public Pitch(Clock clock) {
        super(DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        setOutputLimits(MIN_PITCH_DEG, MAX_PITCH_DEG);
    }

    public Pitch(double kp, double ki, double kd, Clock clock) {
        super(kp, ki, kd, clock);
        setOutputLimits(MIN_PITCH_DEG, MAX_PITCH_DEG);
    }

    /**
     * Computes pitch command based on pitch error.
     * 
     * @param targetPitchDeg  desired pitch angle
     * @param currentPitchDeg current pitch angle
     * @param dt              time delta
     * @return pitch command in degrees
     */
    public double compute(double targetPitchDeg, double currentPitchDeg, double dt) {
        double error = targetPitchDeg - currentPitchDeg;
        return super.compute(error, currentPitchDeg, dt);
    }
}
