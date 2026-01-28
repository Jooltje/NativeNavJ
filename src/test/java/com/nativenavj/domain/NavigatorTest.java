package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NavigatorTest {
    @Test
    void testNavigatorCreation() {
        Navigator nav = new Navigator(true, "TEST");
        assertTrue(nav.active());
        assertEquals("TEST", nav.mode());
    }

    @Test
    void testActiveInactive() {
        Navigator active = Navigator.active("AUTO");
        assertTrue(active.active());
        assertEquals("AUTO", active.mode());

        Navigator inactive = Navigator.inactive();
        assertFalse(inactive.active());
        assertEquals("STANDBY", inactive.mode());
    }
}
