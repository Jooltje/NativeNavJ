package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StateTest {
    @Test
    void testStateCreation() {
        State state = new State(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        assertEquals(1.0, state.latitude());
        assertEquals(2.0, state.longitude());
        assertEquals(3.0, state.heading());
        assertEquals(4.0, state.altitude());
        assertEquals(5.0, state.roll());
        assertEquals(6.0, state.pitch());
        assertEquals(7.0, state.yaw());
        assertEquals(8.0, state.speed());
        assertEquals(9.0, state.climb());
        assertEquals(10.0, state.time());
    }

    @Test
    void testGetters() {
        State state = new State(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        assertEquals(1.0, state.getLatitude());
        assertEquals(4.0, state.getAltitude());
        assertEquals(8.0, state.getSpeed());
    }

    @Test
    void testEquality() {
        State s1 = new State(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        State s2 = new State(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
