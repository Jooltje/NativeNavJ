package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    @Test
    void shouldInitializeWithDefaults() {
        Memory memory = new Memory();
        assertNotNull(memory.getGoal());
        assertNotNull(memory.getState());
        assertNotNull(memory.getNavigator());
        assertNotNull(memory.getAssistant());
    }

    @Test
    void shouldUpdateGoal() {
        Memory memory = new Memory();
        Goal newGoal = new Goal(10000, 150, 90);
        memory.setGoal(newGoal);
        assertEquals(newGoal, memory.getGoal());
    }

    @Test
    void shouldUpdateState() {
        Memory memory = new Memory();
        State stallState = new State(0.0, 0.0, 0.0, 5000.0, 0.0, 10.0, 0.0, 35.0, -200.0, 100.0);
        memory.setState(stallState);
        assertEquals(stallState, memory.getState());
    }

    @Test
    void shouldUpdateNavigator() {
        Memory memory = new Memory();
        Navigator newNavigator = Navigator.active("NAV");
        memory.setNavigator(newNavigator);
        assertEquals(newNavigator, memory.getNavigator());
    }

    @Test
    void shouldUpdateAssistant() {
        Memory memory = new Memory();
        Assistant newAssistant = new Assistant(true, Assistant.Status.IDLE, "");
        memory.setAssistant(newAssistant);
        assertEquals(newAssistant, memory.getAssistant());
    }

    @Test
    void shouldHandleSettings() {
        Memory memory = new Memory();
        assertNotNull(memory.getSettings());
        Settings newSettings = new Settings(
                new Configuration(true, 10.0, 1.0, 0.0, 0.0, -1.0, 1.0),
                Configuration.SURFACE,
                Configuration.SURFACE,
                Configuration.THROTTLE);
        memory.setSettings(newSettings);
        assertEquals(newSettings, memory.getSettings());
    }
}
