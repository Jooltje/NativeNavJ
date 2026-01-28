package com.nativenavj.control;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoopTest {

    @Test
    void shouldStoreStatusAsNoun() {
        Loop loop = new Loop(true, 10.0);
        assertTrue(loop.status());
    }

    @Test
    void shouldStoreFrequency() {
        Loop loop = new Loop(true, 25.0);
        assertEquals(25.0, loop.frequency());
    }

    @Test
    void shouldSupportEquality() {
        Loop l1 = new Loop(true, 10.0);
        Loop l2 = new Loop(true, 10.0);
        assertEquals(l1, l2);
    }
}
