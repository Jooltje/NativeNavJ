package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TargetTest {

    @Test
    void shouldStoreValues() {
        Target target = new Target(10.0, 5.0, 2.0, 0.5);
        assertEquals(10.0, target.roll(), 0.01);
        assertEquals(5.0, target.pitch(), 0.01);
        assertEquals(2.0, target.yaw(), 0.01);
        assertEquals(0.5, target.power(), 0.01);
    }

    @Test
    void shouldProvideNeutralTarget() {
        Target neutral = Target.neutral();
        assertEquals(0.5, neutral.power(), 0.01);
    }
}
