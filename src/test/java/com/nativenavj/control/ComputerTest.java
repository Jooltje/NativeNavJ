package com.nativenavj.control;

import com.nativenavj.domain.State;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for Computer (TECS implementation as Knowledge Source).
 */
class ComputerTest {

    private Memory memory;
    private Computer computer;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        computer = new Computer(memory);
    }

    @Test
    void testSpecificEnergyCalculation() {
        double altitude = 1000.0;
        double airspeed = 100.0;
        double expectedEnergy = computer.calculateSpecificEnergy(altitude, airspeed);
        assertTrue(expectedEnergy > 1000.0);
        assertTrue(expectedEnergy < 1500.0);
    }

    @Test
    void testStallProtection() {
        State stallState = new State(0.0, 0.0, 0.0, 5000.0, 0.0, 10.0, 0.0, 35.0, -200.0, 100.0);
        memory.setState(stallState);
        computer.activate();

        computer.step();

        Target target = memory.getTarget();
        assertTrue(target.pitch() < 0.0, "Should target nose down in stall");
        assertEquals(1.0, target.throttle(), 0.01, "Should target full throttle");
    }

    @Test
    void testSystemAlwaysUpdatesTargetInStep() {
        State state = State.neutral();
        memory.setState(state);

        // Computer should always update target when step is called
        // Orchestrator manages the actual thread execution based on Navigator state
        computer.step();

        Target target = memory.getTarget();
        assertNotEquals(Target.neutral(), target);
    }

    @Test
    void testSetAltitude() {
        computer.setAltitude(8000.0);
        assertEquals(8000.0, memory.getGoal().altitude(), 0.01);
    }

    @Test
    void testSetSpeed() {
        computer.setSpeed(150.0);
        assertEquals(150.0, memory.getGoal().speed(), 0.01);
    }

    @Test
    void testSetHeading() {
        computer.setHeading(270.0);
        assertEquals(270.0, memory.getGoal().heading(), 0.01);
    }

    @Test
    void testActivate() {
        computer.activate();
        assertTrue(memory.getNavigator().active());
    }

    @Test
    void testDeactivate() {
        computer.activate();
        computer.deactivate();
        assertFalse(memory.getNavigator().active());
    }
}
