package com.nativenavj.adapter;

import com.nativenavj.domain.State;
import com.nativenavj.port.Sensor;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Mock sensor for testing.
 * Allows injecting predefined telemetry sequences.
 */
public class MockSensor implements Sensor {
    private final Queue<State> stateQueue = new LinkedList<>();
    private State currentState = State.neutral();
    private boolean available = true;

    /**
     * Enqueues a state to be returned by subsequent read() calls.
     */
    public void enqueue(State state) {
        stateQueue.offer(state);
    }

    /**
     * Sets the current state directly.
     */
    public void setState(State state) {
        this.currentState = state;
    }

    /**
     * Sets the availability status.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public State read() {
        if (!stateQueue.isEmpty()) {
            currentState = stateQueue.poll();
        }
        return currentState;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
