package com.nativenavj.port;

import com.nativenavj.domain.Command;

/**
 * Output port for control commands.
 * Implementations send commands to the aircraft control surfaces.
 */
public interface Actuator {
    /**
     * Writes a control command to the aircraft.
     * 
     * @param command the control surface commands to apply
     */
    void write(Command command);

    /**
     * Checks if the actuator is ready to accept commands.
     * 
     * @return true if actuator is operational
     */
    boolean isReady();
}
