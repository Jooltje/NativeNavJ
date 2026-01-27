package com.nativenavj.adapter;

import com.nativenavj.port.Actuator;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock actuator for testing.
 * Records all commands written for verification in tests.
 */
public class MockActuator implements Actuator {
    private double lastValue = 0.0;
    private final List<Double> callLog = new ArrayList<>();

    @Override
    public void setSignal(double value) {
        this.lastValue = value;
        callLog.add(value);
    }

    public double getLastValue() {
        return lastValue;
    }

    public List<Double> getCallLog() {
        return new ArrayList<>(callLog);
    }
}
