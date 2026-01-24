package com.nativenavj.control.core;

import com.nativenavj.util.LogManager;

/**
 * Generic frequency manager for flight control loops.
 * Implements a precise drift-compensating sleep loop.
 */
public abstract class LoopController implements Runnable {
    private final long periodNs;
    private volatile boolean running = true;

    public LoopController(double hz) {
        this.periodNs = (long) (1_000_000_000.0 / hz);
    }

    /**
     * Abstract hook for the specific module logic.
     */
    protected abstract void step();

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        long nextTickNs = System.nanoTime();

        while (running) {
            try {
                step();
            } catch (Exception e) {
                LogManager.error("Error in LoopController step: " + e.getMessage());
            }

            nextTickNs += periodNs;
            long waitNs = nextTickNs - System.nanoTime();

            if (waitNs > 0) {
                long millis = waitNs / 1_000_000;
                int nanos = (int) (waitNs % 1_000_000);
                try {
                    Thread.sleep(millis, nanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            } else if (waitNs < -periodNs) {
                // If we are more than one period behind, reset nextTickNs to catch up
                // (Drift compensation: usually we want to catch up, but if we're too far
                // behind, we skip)
                nextTickNs = System.nanoTime();
            }
        }
    }
}
