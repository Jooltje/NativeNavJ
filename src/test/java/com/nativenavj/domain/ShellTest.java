package com.nativenavj.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for Shell command parser.
 */
class ShellTest {

    private Memory memory;
    private Shell shell;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        // Empty input stream for direct execute() tests
        shell = new Shell(memory, new java.io.ByteArrayInputStream(new byte[0]));
    }

    @Test
    void testSysOn() {
        // Test SYS ON command activates the computer
        String result = shell.execute("SYS ON");

        assertTrue(memory.getNavigator().active());
        assertTrue(result.contains("ON") || result.contains("enabled"));
    }

    @Test
    void testSysOff() {
        // Test SYS OFF command deactivates the computer
        memory.setNavigator(Navigator.active("AUTONOMOUS"));
        String result = shell.execute("SYS OFF");

        assertFalse(memory.getNavigator().active());
        assertTrue(result.contains("OFF") || result.contains("disabled"));
    }

    @Test
    void testSetHeading() {
        // Test HDG command sets heading
        String result = shell.execute("HDG 180");

        assertEquals(180.0, memory.getGoal().heading(), 0.01);
        assertTrue(result.contains("180"));
    }

    @Test
    void testSetAltitude() {
        // Test ALT command sets altitude
        String result = shell.execute("ALT 5000");

        assertEquals(5000.0, memory.getGoal().altitude(), 0.01);
        assertTrue(result.contains("5000"));
    }

    @Test
    void testSetSpeed() {
        // Test SPD command sets airspeed
        String result = shell.execute("SPD 120");

        assertEquals(120.0, memory.getGoal().speed(), 0.01);
        assertTrue(result.contains("120"));
    }

    @Test
    void testLlmOn() {
        // Test LLM ON command enables LLM mode
        String result = shell.execute("LLM ON");

        assertTrue(shell.isLlm());
        assertTrue(result.contains("ON") || result.contains("enabled"));
    }

    @Test
    void testLlmOff() {
        // Test LLM OFF command disables LLM mode
        shell.execute("LLM ON");
        String result = shell.execute("LLM OFF");

        assertFalse(shell.isLlm());
        assertTrue(result.contains("OFF") || result.contains("disabled"));
    }

    @Test
    void testAskCommand() {
        // Test ASK command returns appropriate message
        String result = shell.execute("ASK Fly to EGJJ");

        assertTrue(result.contains("ASK") || result.contains("prompt") || result.contains("LLM"));
    }

    @Test
    void testInvalidCommand() {
        // Test invalid command returns error message
        String result = shell.execute("INVALID");

        assertTrue(result.contains("Unknown") || result.contains("Invalid") || result.contains("ERROR"));
    }

    @Test
    void testInvalidSysArgument() {
        // Test SYS with invalid argument
        String result = shell.execute("SYS MAYBE");

        assertTrue(result.contains("Invalid") || result.contains("ERROR"));
    }

    @Test
    void testMissingArgument() {
        // Test command with missing argument
        String result = shell.execute("HDG");

        assertTrue(result.contains("Invalid") || result.contains("ERROR") || result.contains("missing"));
    }

    @Test
    void testInvalidNumberArgument() {
        // Test command with invalid number
        String result = shell.execute("ALT abc");

        assertTrue(result.contains("Invalid") || result.contains("ERROR"));
    }

    @Test
    void testHeadingNormalization() {
        // Test heading wraps around 360
        shell.execute("HDG 450");

        double heading = memory.getGoal().heading();
        assertTrue(heading >= 0.0 && heading < 360.0);
    }

    @Test
    void testNegativeHeading() {
        // Test negative heading normalizes correctly
        shell.execute("HDG -90");

        double heading = memory.getGoal().heading();
        assertTrue(heading >= 0.0 && heading < 360.0);
        assertEquals(270.0, heading, 0.01);
    }

    @Test
    void testSetCommand() {
        // Test SET ROL KP 0.5 command
        String result = shell.execute("SET ROL KP 0.5");

        Configuration config = memory.getConfiguration("ROL");
        assertEquals(0.5, config.proportional(), 0.001);
        assertTrue(result.contains("Set ROL KP to 0.5"));
    }

    @Test
    void testSetCommandNewFunction() {
        // Test SET for a new function
        String result = shell.execute("SET NEW SYS ON");

        Configuration config = memory.getConfiguration("NEW");
        assertNotNull(config);
        assertTrue(memory.isActive("NEW"));
        assertTrue(result.contains("Set NEW SYS to ON"));
    }

    @Test
    void testSetCommandInvalidValue() {
        // Test SET with invalid value
        String result = shell.execute("SET ROL FRQ abc");

        assertTrue(result.contains("ERROR"), "Result should contain ERROR: " + result);
        assertTrue(result.contains("Invalid value"), "Result should contain 'Invalid value': " + result);
    }

    @Test
    void testSetCommandInvalidParameter() {
        // Test SET with invalid parameter
        String result = shell.execute("SET ROL UNK 1.0");

        assertTrue(result.contains("ERROR"), "Result should contain ERROR: " + result);
        assertTrue(result.contains("Unknown parameter"), "Result should contain 'Unknown parameter': " + result);
    }

    @Test
    void testSetCommandMissingArguments() {
        // Test SET with missing arguments
        String result = shell.execute("SET ROL KP");

        assertTrue(result.contains("ERROR"), "Result should contain ERROR: " + result);
        assertTrue(result.contains("requires"), "Result should contain 'requires': " + result);
    }

    @Test
    void testCaseInsensitive() {
        // Test commands are case insensitive
        shell.execute("sys on");
        assertTrue(memory.getNavigator().active());

        shell.execute("set pit ki 0.1");
        assertEquals(0.1, memory.getConfiguration("PIT").integral(), 0.001);
    }
}
