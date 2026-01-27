package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;

/**
 * Pitch controller for elevator control.
 * Manages aircraft pitch attitude to achieve target pitch angles.
 */
public class Pitch extends Controller {

    public Pitch(Actuator actuator, Sensor sensor, Memory memory, Configuration configuration) {
        super(memory, actuator, sensor, configuration);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.pitch();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setElevator(output);
    }
}
