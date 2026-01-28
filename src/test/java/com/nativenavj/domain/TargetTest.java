package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TargetTest {
    @Test
    void testTargetCreation() {
        Target target = new Target(10.0, 5.0, 0.0, 0.7);
        assertEquals(10.0, target.roll());
        assertEquals(5.0, target.pitch());
        assertEquals(0.0, target.yaw());
        assertEquals(0.7, target.throttle());
    }

    @Test
    void testGetters() {
        Target target = new Target(10.0, 5.0, 0.0, 0.7);
        assertEquals(10.0, target.getRoll());
        assertEquals(5.0, target.getPitch());
        assertEquals(0.0, target.getYaw());
        assertEquals(0.7, target.getThrottle());
    }

    @Test
    void testNeutral() {
        Target target = Target.neutral();
        assertEquals(0.0, target.roll());
        assertEquals(0.0, target.throttle());
    }
}
