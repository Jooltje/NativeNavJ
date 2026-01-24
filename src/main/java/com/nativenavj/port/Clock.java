package com.nativenavj.port;

/**
 * Time abstraction for deterministic testing.
 * Allows control loops to be tested without real-time dependencies.
 */
public interface Clock {
    /**
     * Returns the current time in nanoseconds.
     * 
     * @return current time in nanoseconds
     */
    long nanoTime();

    /**
     * Sleeps for the specified duration.
     * 
     * @param nanos duration to sleep in nanoseconds
     * @throws InterruptedException if interrupted during sleep
     */
    void sleep(long nanos) throws InterruptedException;
}
