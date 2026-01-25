package com.nativenavj.adapter;

import com.nativenavj.port.Actuator;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock actuator for testing.
 * Records all commands written for verification in tests.
 */
public class MockActuator implements Actuator {
    private double lastAileron = 0.0;
    private double lastElevator = 0.0;
    private double lastRudder = 0.0;
    private double lastThrottle = 0.0;
    private boolean ready = true;

    private final List<String> callLog = new ArrayList<>();

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public void setAileron(double value) {
        this.lastAileron = value;
        callLog.add("aileron:" + value);
    }

    @Override
    public void setElevator(double value) {
        this.lastElevator = value;
        callLog.add("elevator:" + value);
    }

    @Override
    public void setRudder(double value) {
        this.lastRudder = value;
        callLog.add("rudder:" + value);
    }

    @Override
    public void setThrottle(double value) {
        this.lastThrottle = value;
        callLog.add("throttle:" + value);
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    public double getLastAileron() {
        return lastAileron;
    }

    public double getLastElevator() {
        return lastElevator;
    }

    public double getLastRudder() {
        return lastRudder;
    }

    public double getLastThrottle() {
        return lastThrottle;
    }

    public List<String> getCallLog() {
        return new ArrayList<>(callLog);
    }
}
