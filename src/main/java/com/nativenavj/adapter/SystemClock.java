package com.nativenavj.adapter;

import com.nativenavj.port.Clock;

/**
 * System clock implementation using real system time.
 */
public class SystemClock implements Clock {
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public void sleep(long nanos) throws InterruptedException {
        long millis = nanos / 1_000_000;
        int remainingNanos = (int) (nanos % 1_000_000);
        Thread.sleep(millis, remainingNanos);
    }
}
