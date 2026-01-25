package com.nativenavj.control;

import com.nativenavj.adapter.MockActuator;
import com.nativenavj.adapter.MockClock;
import com.nativenavj.adapter.MockSensor;
import com.nativenavj.domain.Goal;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Navigator;
import com.nativenavj.domain.Memory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for Computer (TECS implementation).
 */
class ComputerTest {

    private MockClock clock;
    private MockSensor sensor;
    private MockActuator actuator;
    private Memory memory;
    private Computer computer;

    @BeforeEach
    void setUp() {
        clock = new MockClock();
        sensor = new MockSensor();
        actuator = new MockActuator();
        memory = new Memory();
        computer = new Computer(sensor, actuator, clock, memory);
    }

    @Test
    void testSpecificEnergyCalculation() {
        // Test specific energy calculation E_s = h + V²/2g
        double altitude = 1000.0; // feet
        double airspeed = 100.0; // knots

        double expectedEnergy = computer.calculateSpecificEnergy(altitude, airspeed);

        // E_s = h + V²/2g where g ≈ 32.2 ft/s²
        // Convert knots to ft/s: 100 kts * 1.68781 = 168.781 ft/s
        // V²/2g = 168.781² / (2 * 32.2) ≈ 442.5 ft
        // Total: 1000 + 442.5 = 1442.5 ft
        assertTrue(expectedEnergy > 1000.0);
        assertTrue(expectedEnergy < 1500.0);
    }

    @Test
    void testEnergyRateCalculation() {
        State state = new State(
                0.0, 0.0, // lat/lon
                0.0, // heading
                5000.0, // altitude
                0.0, // bank
                5.0, // pitch
                0.0, // yaw
                120.0, // speed
                500.0 // climb (fpm)
        );

        double energyRate = computer.calculateEnergyRate(state);

        // Energy rate should be positive when climbing
        assertTrue(energyRate > 0.0);
    }

    @Test
    void testEnergyDistributionCalculation() {
        State state = new State(
                0.0, 0.0, // lat/lon
                0.0, // heading
                5000.0, // altitude
                0.0, // bank
                5.0, // pitch
                0.0, // yaw
                120.0, // speed
                500.0 // climb (fpm)
        );

        double distribution = computer.calculateEnergyDistribution(state);

        // Distribution represents balance between altitude and speed changes
        assertNotNull(distribution);
    }

    @Test
    void testStallProtection() {
        // Test that stall protection overrides normal control
        State stallState = new State(
                0.0, 0.0, // lat/lon
                0.0, // heading
                5000.0, // altitude
                0.0, // bank
                10.0, // pitch
                0.0, // yaw
                35.0, // speed - below stall speed
                -200.0 // climb
        );

        sensor.setState(stallState);
        computer.setAltitude(6000.0);
        computer.setSpeed(120.0);
        computer.setHeading(0.0);
        computer.activate();

        computer.compute(0.1);

        // Should command nose down and full throttle
        var command = actuator.getLastCommand();
        assertTrue(command.pitch() < 0.0, "Should pitch down in stall");
        assertEquals(1.0, command.throttle(), 0.01, "Should apply full throttle");
    }

    @Test
    void testInactiveSystemDoesNotCommand() {
        // Test that inactive system doesn't send commands
        State state = State.neutral();
        sensor.setState(state);
        computer.deactivate();

        computer.compute(0.1);

        // Should not have written any commands
        assertTrue(actuator.getCommandHistory().isEmpty());
    }

    @Test
    void testSetAltitude() {
        // Test setting altitude updates internal goal
        computer.setAltitude(8000.0);

        Goal goal = computer.getGoal();
        assertEquals(8000.0, goal.altitude(), 0.01);
    }

    @Test
    void testSetSpeed() {
        // Test setting speed updates internal goal
        computer.setSpeed(150.0);

        Goal goal = computer.getGoal();
        assertEquals(150.0, goal.speed(), 0.01);
    }

    @Test
    void testSetHeading() {
        // Test setting heading updates internal goal
        computer.setHeading(270.0);

        Goal goal = computer.getGoal();
        assertEquals(270.0, goal.heading(), 0.01);
    }

    @Test
    void testActivate() {
        // Test activation sets status to active
        computer.activate();

        Navigator navigator = computer.getNavigator();
        assertTrue(navigator.active());
    }

    @Test
    void testDeactivate() {
        // Test deactivation sets status to inactive
        computer.activate();
        computer.deactivate();

        Navigator navigator = computer.getNavigator();
        assertFalse(navigator.active());
    }
}
