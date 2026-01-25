package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;

/**
 * Throttle controller for engine power control.
 * Manages engine throttle setting based on target percentage.
 */
public class Throttle extends Controller {

    private static final double DEFAULT_KP = 1.0;
    private static final double DEFAULT_KI = 0.0;
    private static final double DEFAULT_KD = 0.0;

    public Throttle(Actuator actuator, Memory memory, Clock clock) {
        super(50.0, memory, actuator, DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        this.setOutputLimits(0.0, 1.0);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.throttle();
    }

    @Override
    protected double getFeedback(State state) {
        // No feedback available in current State for throttle position
        return 0.0;
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setThrottle(output);
    }
}
