package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
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

    public Roll(Actuator actuator, Memory memory, Clock clock) {
        super(50.0, memory, actuator, DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        this.setOutputLimits(MIN_ROLL_DEG, MAX_ROLL_DEG);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.roll();
    }

    @Override
    protected double getFeedback(State state) {
        return state.roll();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setAileron(output / 30.0);
    }
}
