package com.nativenavj.port;

/**
 * Port for aircraft control surface actuation.
 */
public interface Actuator {
    /**
     * Sets a normalized control signal.
     * 
     * @param value normalized value (typically [-1.0, 1.0] or [0.0, 1.0])
     */
    void setSignal(double value);
}
