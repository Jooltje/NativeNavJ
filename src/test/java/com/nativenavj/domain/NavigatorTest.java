package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NavigatorTest {
    @Test
    void shouldCreateNavigatorWithActiveStatus() {
        Navigator nav = new Navigator(true, "TEST");
        assertTrue(nav.status());
    }

    @Test
    void shouldCreateNavigatorWithCorrectMode() {
        Navigator nav = new Navigator(true, "TEST");
        assertEquals("TEST", nav.mode());
    }

    @Test
    void shouldCreateActiveNavigatorWithCorrectMode() {
        Navigator active = Navigator.active("AUTO");
        assertTrue(active.status());
        assertEquals("AUTO", active.mode());
    }

    @Test
    void shouldCreateInactiveNavigatorWithDefaultMode() {
        Navigator inactive = Navigator.inactive();
        assertFalse(inactive.status());
        assertEquals("STANDBY", inactive.mode());
    }
}
