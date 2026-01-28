package com.nativenavj.control;

/**
 * Generic frequency-managed loop state for control systems.
 * 
 * @param active    whether the loop is currently running
 * @param frequency frequency in hertz
 */
public record Loop(boolean status, double frequency) {
}
