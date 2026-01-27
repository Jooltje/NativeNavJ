package com.nativenavj.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic frequency-managed loop for control systems.
 * Provides the core step logic to be executed by a scheduler.
 */
public abstract class Loop implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Loop.class);

    protected final long periodNanos;

    /**
     * Creates a new loop with specified frequency.
     * 
     * @param hz frequency in hertz
     */
    public Loop(double hz) {
        this.periodNanos = (long) (1_000_000_000.0 / hz);
    }

    /**
     * Abstract hook for the specific loop logic.
     */
    protected abstract void step();

    /**
     * Wrapper for the step logic that handles errors.
     */
    public void executeStep() {
        try {
            step();
        } catch (Exception e) {
            log.error("Error in Loop step: {}", e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        executeStep();
    }

    public long getPeriodNanos() {
        return periodNanos;
    }
}
