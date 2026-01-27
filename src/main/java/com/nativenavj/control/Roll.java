package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;

/**
 * Roll controller for aileron control.
 * Manages aircraft roll attitude to achieve target bank angles.
 */
public class Roll extends Controller {

    public Roll(Actuator actuator, Sensor sensor, Memory memory, Configuration configuration) {
        super(memory, actuator, sensor, configuration);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.roll();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setAileron(output);
    }
}
