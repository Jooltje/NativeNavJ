package com.nativenavj.domain;

import com.nativenavj.adapter.MockActuator;
import com.nativenavj.adapter.MockClock;
import com.nativenavj.adapter.MockSensor;
import com.nativenavj.control.Computer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for Shell command parser.
 */
class ShellTest {

    private Computer computer;
    private Shell shell;

    @BeforeEach
    void setUp() {
        MockClock clock = new MockClock();
        MockSensor sensor = new MockSensor();
        MockActuator actuator = new MockActuator();
        computer = new Computer(sensor, actuator, clock);
        shell = new Shell(computer);
    }

    @Test
    void testSysOn() {
        // Test SYS ON command activates the computer
        String result = shell.execute("SYS ON");

        assertTrue(computer.getStatus().active());
        assertTrue(result.contains("ON") || result.contains("enabled"));
    }

    @Test
    void testSysOff() {
        // Test SYS OFF command deactivates the computer
        computer.activate();
        String result = shell.execute("SYS OFF");

        assertFalse(computer.getStatus().active());
        assertTrue(result.contains("OFF") || result.contains("disabled"));
    }

    @Test
    void testSetHeading() {
        // Test HDG command sets heading
        String result = shell.execute("HDG 180");

        assertEquals(180.0, computer.getGoal().heading(), 0.01);
        assertTrue(result.contains("180"));
    }

    @Test
    void testSetAltitude() {
        // Test ALT command sets altitude
        String result = shell.execute("ALT 5000");

        assertEquals(5000.0, computer.getGoal().altitude(), 0.01);
        assertTrue(result.contains("5000"));
    }

    @Test
    void testSetSpeed() {
        // Test SPD command sets airspeed
        String result = shell.execute("SPD 120");

        assertEquals(120.0, computer.getGoal().speed(), 0.01);
        assertTrue(result.contains("120"));
    }

    @Test
    void testLlmOn() {
        // Test LLM ON command enables LLM mode
        String result = shell.execute("LLM ON");

        assertTrue(shell.isLlmEnabled());
        assertTrue(result.contains("ON") || result.contains("enabled"));
    }

    @Test
    void testLlmOff() {
        // Test LLM OFF command disables LLM mode
        shell.execute("LLM ON");
        String result = shell.execute("LLM OFF");

        assertFalse(shell.isLlmEnabled());
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

        double heading = computer.getGoal().heading();
        assertTrue(heading >= 0.0 && heading < 360.0);
    }

    @Test
    void testNegativeHeading() {
        // Test negative heading normalizes correctly
        shell.execute("HDG -90");

        double heading = computer.getGoal().heading();
        assertTrue(heading >= 0.0 && heading < 360.0);
        assertEquals(270.0, heading, 0.01);
    }

    @Test
    void testCaseInsensitive() {
        // Test commands are case insensitive
        String result = shell.execute("sys on");

        assertTrue(computer.getStatus().active());
    }

    @Test
    void testExtraWhitespace() {
        // Test command handles extra whitespace
        String result = shell.execute("  HDG   180  ");

        assertEquals(180.0, computer.getGoal().heading(), 0.01);
    }
}
