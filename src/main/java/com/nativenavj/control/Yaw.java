package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;

/**
 * Yaw controller for rudder control.
 * Manages aircraft slip and turn coordination.
 */
public class Yaw extends Controller {

    private static final double DEFAULT_KP = 0.3;
    private static final double DEFAULT_KI = 0.01;
    private static final double DEFAULT_KD = 0.05;

    private static final double MIN_RUDDER_DEG = -30.0;
    private static final double MAX_RUDDER_DEG = 30.0;

    public Yaw(Actuator actuator, Memory memory, Clock clock) {
        super(50.0, memory, actuator, DEFAULT_KP, DEFAULT_KI, DEFAULT_KD, clock);
        this.setOutputLimits(MIN_RUDDER_DEG, MAX_RUDDER_DEG);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.yaw();
    }

    @Override
    protected double getFeedback(State state) {
        return state.yaw();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setRudder(output / 30.0);
    }
}
