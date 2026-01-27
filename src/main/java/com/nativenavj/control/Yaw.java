package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;

/**
 * Yaw controller for rudder control.
 * Manages aircraft heading through rudder input.
 */
public class Yaw extends Controller {

    public Yaw(Actuator actuator, Sensor sensor, Memory memory, Configuration configuration) {
        super(memory, actuator, sensor, configuration);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.yaw();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setRudder(output);
    }
}
