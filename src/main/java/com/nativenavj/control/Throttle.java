package com.nativenavj.control;

import com.nativenavj.port.Clock;

/**
 * Throttle controller for engine power control.
 * Manages throttle based on TECS energy commands.
 */
public class Throttle extends Controller {
    private static final double DEFAULT_KP = 0.4;
    private static final double DEFAULT_KI = 0.08;
    private static final double DEFAULT_KD = 0.02;

    private static final double MIN_THROTTLE = 0.0;
    private static final double MAX_THROTTLE = 1.0;

    public Throttle(Clock clock) {
        super(DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        setOutputLimits(MIN_THROTTLE, MAX_THROTTLE);
    }

    public Throttle(double kp, double ki, double kd, Clock clock) {
        super(kp, ki, kd, clock);
        setOutputLimits(MIN_THROTTLE, MAX_THROTTLE);
    }

    /**
     * Computes throttle command based on energy error.
     * 
     * @param energyError     total energy error from TECS
     * @param currentThrottle current throttle position
     * @param dt              time delta
     * @return throttle command [0.0, 1.0]
     */
    public double compute(double energyError, double currentThrottle, double dt) {
        return compute(energyError, currentThrottle, dt);
    }
}
