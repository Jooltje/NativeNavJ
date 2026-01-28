package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GoalTest {

    @Test
    void shouldStoreHeight() {
        Goal goal = new Goal(10000.0, 150.0, 90.0);
        assertEquals(10000.0, goal.height());
    }

    @Test
    void shouldStoreVelocity() {
        Goal goal = new Goal(10000.0, 150.0, 90.0);
        assertEquals(150.0, goal.velocity());
    }

    @Test
    void shouldStoreDirection() {
        Goal goal = new Goal(10000.0, 150.0, 90.0);
        assertEquals(90.0, goal.direction());
    }

    @Test
    void shouldHaveDefaultObjective() {
        assertNotNull(Goal.DEFAULT);
    }
}
