package com.nativenavj.control.core;

/**
 * Produced by: TECS
 * Consumed by: PID Loops
 */
public record ControlFrame(
        double pitchTargetDeg, // Calculated by Energy Balance
        double rollTargetDeg, // Calculated by Nav Logic
        double throttlePercent // Calculated by Total Energy
) {
}
