package com.nativenavj.control;

import com.nativenavj.port.Clock;

/**
 * Generic frequency-managed loop for control systems.
 * Implements precise drift-compensating timing using Clock abstraction.
 */
public abstract class Loop implements Runnable {
    private final long periodNanos;
    private final Clock clock;
    private volatile boolean running = true;

    /**
     * Creates a new loop with specified frequency.
     * 
     * @param hz    frequency in hertz
     * @param clock time source for deterministic testing
     */
    public Loop(double hz, Clock clock) {
        this.periodNanos = (long) (1_000_000_000.0 / hz);
        this.clock = clock;
    }

    /**
     * Abstract hook for the specific loop logic.
     */
    protected abstract void step();

    /**
     * Stops the loop.
     */
    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        long nextTickNanos = clock.nanoTime();

        while (running) {
            try {
                step();
            } catch (Exception e) {
                // Log error but continue running
                System.err.println("Error in Loop step: " + e.getMessage());
            }

            nextTickNanos += periodNanos;
            long waitNanos = nextTickNanos - clock.nanoTime();

            if (waitNanos > 0) {
                try {
                    clock.sleep(waitNanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            } else if (waitNanos < -periodNanos) {
                // If we are more than one period behind, reset to catch up
                nextTickNanos = clock.nanoTime();
            }
        }
    }
}
