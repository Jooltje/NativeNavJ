package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

class OrchestratorConcurrencyTest {

    private Memory memory;
    private Orchestrator orchestrator;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        orchestrator = new Orchestrator(
                memory,
                mock(com.nativenavj.adapter.Connector.class),
                mock(com.nativenavj.control.Computer.class),
                mock(com.nativenavj.domain.Shell.class),
                mock(Runnable.class));
    }

    @Test
    void shouldHandleConcurrentConfigurationChanges() throws InterruptedException {
        int threads = 10;
        int iterations = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int tid = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        orchestrator.configure("TASK_" + tid, null, (double) j, true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Threads did not complete in time");
        executor.shutdown();

        // This test aims to trigger ConcurrentModificationException or similar if maps
        // are not thread-safe
        // during configure() and potentially internal execution if we ran it.
        // For now, configure() is the primary entry point for mutation.
    }
}
