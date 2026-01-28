package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GoalTest {
    @Test
    void testGoalCreation() {
        Goal goal = new Goal(1000.0, 150.0, 180.0);
        assertEquals(1000.0, goal.altitude());
        assertEquals(150.0, goal.speed());
        assertEquals(180.0, goal.heading());
    }

    @Test
    void testGetters() {
        Goal goal = new Goal(1000.0, 150.0, 180.0);
        assertEquals(1000.0, goal.getAltitude());
        assertEquals(150.0, goal.getSpeed());
        assertEquals(180.0, goal.getHeading());
    }

    @Test
    void testEquality() {
        Goal g1 = new Goal(1000, 150, 180);
        Goal g2 = new Goal(1000, 150, 180);
        assertEquals(g1, g2);
    }
}
