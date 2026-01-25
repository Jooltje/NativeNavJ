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
        State newState = new State(1, 2, 3, 4, 5, 6, 7, 8, 9);
        memory.setState(newState);
        assertEquals(newState, memory.getState());
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
        Assistant newAssistant = new Assistant(true, "WORKING");
        memory.setAssistant(newAssistant);
        assertEquals(newAssistant, memory.getAssistant());
    }
}
