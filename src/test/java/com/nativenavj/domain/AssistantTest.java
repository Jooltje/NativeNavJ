package com.nativenavj.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssistantTest {
    @Test
    void testAssistantCreation() {
        Assistant assistant = new Assistant(true, Assistant.Status.THINKING, "Hello");
        assertTrue(assistant.activity());
        assertEquals(Assistant.Status.THINKING, assistant.status());
        assertEquals("Hello", assistant.prompt());
    }

    @Test
    void testInactive() {
        Assistant assistant = Assistant.inactive();
        assertFalse(assistant.activity());
        assertEquals(Assistant.Status.IDLE, assistant.status());
    }
}
