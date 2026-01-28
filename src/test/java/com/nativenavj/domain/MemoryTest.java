package com.nativenavj.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    private Memory memory;

    @BeforeEach
    void setUp() {
        memory = new Memory();
    }

    @Test
    void shouldInitializeWithDefaultGoal() {
        assertNotNull(memory.getGoal());
    }

    @Test
    void shouldInitializeWithDefaultState() {
        assertNotNull(memory.getState());
    }

    @Test
    void shouldInitializeWithInactiveNavigator() {
        assertNotNull(memory.getNavigator());
        assertFalse(memory.getNavigator().status());
    }

    @Test
    void shouldInitializeWithInactiveAssistant() {
        assertNotNull(memory.getAssistant());
        assertFalse(memory.getAssistant().activity());
    }

    @Test
    void shouldUpdateGoal() {
        Goal newGoal = new Goal(10000, 150, 90);
        memory.setGoal(newGoal);
        assertEquals(newGoal, memory.getGoal());
    }

    @Test
    void shouldUpdateState() {
        State stallState = new State(0.0, 0.0, 0.0, 5000.0, 0.0, 10.0, 0.0, 35.0, -200.0, 100.0);
        memory.setState(stallState);
        assertEquals(stallState, memory.getState());
    }

    @Test
    void shouldUpdateNavigator() {
        Navigator newNavigator = Navigator.active("NAV");
        memory.setNavigator(newNavigator);
        assertEquals(newNavigator, memory.getNavigator());
    }

    @Test
    void shouldUpdateAssistant() {
        Assistant newAssistant = new Assistant(true, Assistant.Status.IDLE, "");
        memory.setAssistant(newAssistant);
        assertEquals(newAssistant, memory.getAssistant());
    }

    @Test
    void shouldStoreConfigurationByProfile() {
        Configuration newConfig = new Configuration(2.0, 0.5, 0.1, -2.0, 2.0);
        memory.setProfile("CPU", newConfig);
        assertEquals(newConfig, memory.getProfile("CPU"));
    }

    @Test
    void shouldReturnNullForMissingProfile() {
        assertNull(memory.getProfile("NON_EXISTENT"));
    }

    @Test
    void shouldHandleConcurrentProfileUpdates() throws InterruptedException {
        int threads = 10;
        int iterations = 1000;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int tid = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        memory.setProfile("P" + tid, new Configuration(j, 0, 0, -1, 1));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS));
        executor.shutdown();
    }
}
