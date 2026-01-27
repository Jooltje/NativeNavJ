package com.nativenavj.domain;

/**
 * A Sample represents a single measurement at a specific simulator time.
 * This is an immutable data object.
 */
public record Sample(double time, double value) {
    public double getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }
}
