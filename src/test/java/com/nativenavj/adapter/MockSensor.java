package com.nativenavj.adapter;

import com.nativenavj.domain.Telemetry;
import com.nativenavj.port.Sensor;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Mock sensor for testing.
 * Allows injecting predefined telemetry sequences.
 */
public class MockSensor implements Sensor {
    private final Queue<Telemetry> telemetryQueue = new LinkedList<>();
    private Telemetry currentTelemetry = Telemetry.neutral();
    private boolean available = true;

    /**
     * Enqueues a telemetry to be returned by subsequent read() calls.
     */
    public void enqueue(Telemetry telemetry) {
        telemetryQueue.offer(telemetry);
    }

    /**
     * Sets the current telemetry directly.
     */
    public void setTelemetry(Telemetry telemetry) {
        this.currentTelemetry = telemetry;
    }

    /**
     * Sets the availability status.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public Telemetry read() {
        if (!telemetryQueue.isEmpty()) {
            currentTelemetry = telemetryQueue.poll();
        }
        return currentTelemetry;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
