package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;

/**
 * Throttle controller for engine power control.
 * Manages engine throttle setting based on target percentage.
 */
public class Throttle extends Controller {

    public Throttle(Actuator actuator, Sensor sensor, Memory memory, Configuration configuration) {
        super(memory, actuator, sensor, configuration);
    }

    @Override
    protected double getSetpoint(Target target) {
        return target.throttle();
    }

    @Override
    protected void sendCommand(double output) {
        actuator.setThrottle(output);
    }
}
