package com.nativenavj.port;

import com.nativenavj.domain.State;

/**
 * Input port for aircraft telemetry.
 * Implementations provide access to current aircraft state.
 */
public interface Sensor {
    /**
     * Reads the current aircraft state.
     * 
     * @return current telemetry state
     */
    State read();

    /**
     * Checks if the sensor is available and providing data.
     * 
     * @return true if sensor is operational
     */
    boolean isAvailable();
}
