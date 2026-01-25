package com.nativenavj.port;

/**
 * Port for aircraft control surface actuation.
 */
public interface Actuator {
    /**
     * Set the aileron position.
     * 
     * @param value normalized position [-1.0, 1.0]
     */
    void setAileron(double value);

    /**
     * Set the elevator position.
     * 
     * @param value normalized position [-1.0, 1.0]
     */
    void setElevator(double value);

    /**
     * Set the rudder position.
     * 
     * @param value normalized position [-1.0, 1.0]
     */
    void setRudder(double value);

    /**
     * Set the throttle position.
     * 
     * @param value normalized position [0.0, 1.0]
     */
    void setThrottle(double value);

    /**
     * Check if the actuator is ready to receive commands.
     * 
     * @return true if ready
     */
    boolean isReady();
}
