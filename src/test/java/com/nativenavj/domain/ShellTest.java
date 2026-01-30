package com.nativenavj.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    private Memory memory;
    private Shell shell;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        shell = new Shell(memory, new ByteArrayInputStream(new byte[0]));
    }

    @Test
    void shouldActivateNavigatorOnSysOn() {
        shell.execute("SYS ON");
        assertTrue(memory.getNavigator().status());
        assertTrue(memory.isActive("COMPUTER"));
        assertTrue(memory.isActive("PITCH"));
    }

    @Test
    void shouldReturnSuccessMessageOnSysOn() {
        String result = shell.execute("SYS ON");
        assertTrue(result.contains("All systems enabled"));
    }

    @Test
    void shouldDeactivateNavigatorOnSysOff() {
        shell.execute("SYS ON");
        shell.execute("SYS OFF");
        assertFalse(memory.getNavigator().status());
    }

    @Test
    void shouldUpdateGoalDirectionOnHdgCommand() {
        shell.execute("HDG 180");
        assertEquals(180.0, memory.getGoal().direction(), 0.1);
    }

    @Test
    void shouldUpdateGoalHeightOnAltCommand() {
        shell.execute("ALT 5000");
        assertEquals(5000.0, memory.getGoal().height(), 0.1);
    }

    @Test
    void shouldUpdateGoalVelocityOnSpdCommand() {
        shell.execute("SPD 120");
        assertEquals(120.0, memory.getGoal().velocity(), 0.1);
    }

    @Test
    void shouldEnableAssistantOnLlmOn() {
        shell.execute("LLM ON");
        assertTrue(shell.isAssistant());
    }

    @Test
    void shouldUpdateAssistantStatusOnAsk() {
        shell.execute("LLM ON");
        shell.execute("ASK HELLO");
        assertTrue(memory.getAssistant().activity());
        assertEquals(Assistant.Status.THINKING, memory.getAssistant().status());
    }

    @Test
    void shouldNormalizeHeadingCommand() {
        shell.execute("HDG 450");
        assertEquals(90.0, memory.getGoal().direction(), 0.1);
    }

    @Test
    void shouldHandleNegativeHeadingCommand() {
        shell.execute("HDG -90");
        assertEquals(270.0, memory.getGoal().direction(), 0.1);
    }

    @Test
    void shouldUpdateProfileOnSetCommand() {
        shell.execute("SET ROL KP 0.5");
        assertEquals(0.5, memory.getProfile("ROLL").proportion(), 0.001);
    }

    @Test
    void shouldUpdateScheduleOnSetSysCommand() {
        shell.execute("SET PIT SYS ON");
        assertTrue(memory.isActive("PITCH"));
    }

    @Test
    void shouldUpdateFrequencyOnSetFrqCommand() {
        shell.execute("SET YAW FRQ 25");
        assertEquals(25.0, memory.getFrequency("YAW"), 0.01);
    }
}
