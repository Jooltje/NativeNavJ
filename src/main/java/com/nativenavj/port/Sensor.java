package com.nativenavj.port;

import com.nativenavj.domain.Telemetry;

/**
 * Input port for aircraft telemetry.
 * Implementations provide access to current aircraft telemetry.
 */
public interface Sensor {
    /**
     * Reads the current aircraft telemetry.
     * 
     * @return current telemetry
     */
    Telemetry read();

    /**
     * Checks if the sensor is available and providing data.
     * 
     * @return true if sensor is operational
     */
    boolean isAvailable();
}
