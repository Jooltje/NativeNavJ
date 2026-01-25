package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    @Test
    void shouldInitializeWithDefaults() {
        Memory memory = new Memory();
        assertNotNull(memory.goal());
        assertNotNull(memory.state());
        assertNotNull(memory.navigator());
        assertNotNull(memory.assistant());
    }

    @Test
    void shouldUpdateGoal() {
        Memory memory = new Memory();
        Goal newGoal = new Goal(10000, 150, 90);
        memory.updateGoal(newGoal);
        assertEquals(newGoal, memory.goal());
    }

    @Test
    void shouldUpdateState() {
        Memory memory = new Memory();
        State newState = new State(1, 2, 3, 4, 5, 6, 7, 8, 9);
        memory.updateState(newState);
        assertEquals(newState, memory.state());
    }

    @Test
    void shouldUpdateNavigator() {
        Memory memory = new Memory();
        Navigator newNavigator = Navigator.active("NAV");
        memory.updateNavigator(newNavigator);
        assertEquals(newNavigator, memory.navigator());
    }

    @Test
    void shouldUpdateAssistant() {
        Memory memory = new Memory();
        Assistant newAssistant = new Assistant(true, "WORKING");
        memory.updateAssistant(newAssistant);
        assertEquals(newAssistant, memory.assistant());
    }
}
