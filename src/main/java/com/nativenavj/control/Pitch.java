package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
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

    public Pitch(Actuator actuator, Memory memory, Clock clock) {
        super(50.0, memory, actuator, DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        this.setOutputLimits(MIN_PITCH_DEG, MAX_PITCH_DEG);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.pitch();
    }

    @Override
    protected double getFeedback(State state) {
        return state.pitch();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setElevator(output / 20.0);
    }
}
