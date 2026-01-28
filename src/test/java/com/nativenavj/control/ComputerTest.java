package com.nativenavj.control;

import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComputerTest {

    private Memory memory;
    private Computer computer;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        computer = new Computer(memory);
    }

    @Test
    void shouldSetAltitude() {
        computer.setAltitude(10000.0);
        assertEquals(10000.0, memory.getGoal().height());
    }

    @Test
    void shouldSetSpeed() {
        computer.setSpeed(150.0);
        assertEquals(150.0, memory.getGoal().velocity());
    }

    @Test
    void shouldSetHeading() {
        computer.setHeading(90.0);
        assertEquals(90.0, memory.getGoal().direction());
    }

    @Test
    void shouldCalculateTotalEnergy() {
        double energy = computer.calculateSpecificEnergy(1000.0, 100.0);
        assertTrue(energy > 1000.0);
    }

    @Test
    void shouldCalculateEnergyRate() {
        State state = new State(0, 0, 0, 1000, 0, 0, 0, 100, 500, 0);
        double rate = computer.calculateEnergyRate(state);
        assertTrue(rate > 0);
    }

    @Test
    void shouldCalculateEnergyDistribution() {
        State state = new State(0, 0, 0, 1000, 0, 0, 0, 100, 500, 0);
        double distribution = computer.calculateEnergyDistribution(state);
        assertTrue(distribution > 0);
    }

    @Test
    void shouldUpdateTargetOnEveryCycle() {
        memory.setState(State.neutral());
        computer.activate();
        computer.run();
        assertNotEquals(Target.neutral(), memory.getTarget());
    }

    @Test
    void shouldApplyStallProtectionOnLowSpeed() {
        State state = new State(0, 0, 0, 1000, 0, 0, 0, 50.0, 0, 0);
        memory.setState(state);
        computer.activate();
        computer.run();
        Target target = memory.getTarget();
        assertEquals(-10.0, target.pitch());
        assertEquals(1.0, target.power());
    }

    @Test
    void shouldNotUpdateTargetWhenInactive() {
        memory.setState(State.neutral());
        computer.deactivate();
        computer.run();
        assertEquals(Target.neutral(), memory.getTarget());
    }
}
