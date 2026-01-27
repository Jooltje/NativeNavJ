package com.nativenavj.port;

import com.nativenavj.domain.Sample;

/**
 * Sensor Port interface for reading single-value samples from the simulator.
 */
public interface Sensor {
    /**
     * Returns the latest sample from the simulator.
     * 
     * @return a Sample containing value and simulator time
     */
    Sample getSample();
}
