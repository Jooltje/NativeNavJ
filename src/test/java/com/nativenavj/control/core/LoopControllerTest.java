package com.nativenavj.control.core;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoopControllerTest {

    @Test
    public void testFrequencyStability() throws InterruptedException {
        double targetHz = 50.0;
        AtomicInteger count = new AtomicInteger(0);

        LoopController loop = new LoopController(targetHz) {
            @Override
            protected void step() {
                count.incrementAndGet();
            }
        };

        Thread thread = new Thread(loop);
        thread.start();

        // Run for 1.1 seconds to allow for startup jitter
        Thread.sleep(1100);
        loop.stop();
        thread.join(500);

        int observed = count.get();
        // Expecting ~50-55 calls (allowing for the extra 0.1s + some jitter)
        System.out.println("Observed ticks: " + observed);
        assertTrue(observed >= 50 && observed <= 60, "Expected ~50 ticks, but got " + observed);
    }
}
