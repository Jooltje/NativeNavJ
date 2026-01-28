package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StateTest {

    @Test
    void shouldStoreCoordinates() {
        State state = new State(49.0, -2.0, 180.0, 5000.0, 0, 0, 0, 120.0, 0, 100.0);
        assertEquals(49.0, state.latitude());
        assertEquals(-2.0, state.longitude());
    }

    @Test
    void shouldStoreAttitude() {
        State state = new State(0, 0, 180.0, 0, 10.0, 5.0, 0, 0, 0, 0);
        assertEquals(180.0, state.heading());
        assertEquals(10.0, state.roll());
        assertEquals(5.0, state.pitch());
    }

    @Test
    void shouldStorePerformance() {
        State state = new State(0, 0, 0, 5000.0, 0, 0, 0, 120.0, 500.0, 100.0);
        assertEquals(5000.0, state.altitude());
        assertEquals(120.0, state.speed());
        assertEquals(500.0, state.climb());
    }

    @Test
    void shouldProvideNeutralState() {
        State neutral = State.neutral();
        assertEquals(0, neutral.altitude());
    }
}
