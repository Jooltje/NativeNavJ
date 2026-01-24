package com.nativenavj.adapter;

import com.nativenavj.port.Clock;

/**
 * Mock clock for deterministic testing.
 * Allows controlling time progression in tests.
 */
public class MockClock implements Clock {
    private long currentNanos = 0;

    /**
     * Advances the clock by the specified duration.
     */
    public void advance(long nanos) {
        currentNanos += nanos;
    }

    /**
     * Sets the clock to a specific time.
     */
    public void setTime(long nanos) {
        currentNanos = nanos;
    }

    @Override
    public long nanoTime() {
        return currentNanos;
    }

    @Override
    public void sleep(long nanos) throws InterruptedException {
        // Mock sleep doesn't actually sleep, just advances time
        currentNanos += nanos;
    }
}
