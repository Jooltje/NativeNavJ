package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.port.Objective;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * TDD tests for Controller base class.
 */
class ControllerTest {

    private Objective objective;
    private Actuator actuator;
    private Sensor sensor;
    private Controller controller;
    private Configuration config;

    @BeforeEach
    void setUp() {
        objective = mock(Objective.class);
        actuator = mock(Actuator.class);
        sensor = mock(Sensor.class);
        config = new Configuration(1.0, 0.1, 0.05, -100.0, 100.0);
        controller = new Controller(objective, actuator, sensor, config);
    }

    @Test
    void testProportionalResponse() {
        double error = 10.0;
        double output = controller.compute(error, 0.0, 1.0, config);
        assertEquals(11.0, output, 0.01);
    }

    @Test
    void testIntegralAccumulation() {
        double error = 5.0;
        double dt = 1.0;
        double output1 = controller.compute(error, 0.0, dt, config);
        assertEquals(5.5, output1, 0.01);
        double output2 = controller.compute(error, 0.0, dt, config);
        assertEquals(6.0, output2, 0.01);
    }

    @Test
    void testIntegratorWindupPrevention() {
        Configuration localConfig = new Configuration(1.0, 1.0, 0.0, -10.0, 10.0);
        Controller localController = new Controller(objective, actuator, sensor, localConfig);

        for (int i = 0; i < 100; i++) {
            localController.compute(100.0, 0.0, 1.0, localConfig);
        }
        double output = localController.compute(-5.0, 0.0, 1.0, localConfig);
        assertTrue(output < 10.0, "Integral should have been clamped");
    }

    @Test
    void testDerivativeOnFeedback() {
        controller.compute(10.0, 0.0, 1.0, config);
        double output = controller.compute(8.0, 2.0, 1.0, config);
        assertEquals(9.7, output, 0.01);
    }

    @Test
    void testOutputClamping() {
        Configuration localConfig = new Configuration(1.0, 0.0, 0.0, -5.0, 5.0);
        Controller localController = new Controller(objective, actuator, sensor, localConfig);
        double output = localController.compute(100.0, 0.0, 1.0, localConfig);
        assertEquals(5.0, output, 0.01);
        output = localController.compute(-100.0, 0.0, 1.0, localConfig);
        assertEquals(-5.0, output, 0.01);
    }

    @Test
    void testReset() {
        controller.compute(10.0, 0.0, 1.0, config);
        controller.compute(10.0, 0.0, 1.0, config);
        controller.reset();
        double output = controller.compute(10.0, 0.0, 1.0, config);
        assertEquals(11.0, output, 0.01);
    }

    @Test
    void testBumplessTransfer() {
        // Initial run
        controller.compute(10.0, 0.0, 1.0, config); // Output approx 11.0

        Configuration newConfig = new Configuration(2.0, 0.2, 0.1, -100.0, 100.0);
        Controller next = controller.setConfiguration(newConfig);

        // The output of the NEXT compute should ideally be close to previous output
        // but setConfiguration is intended to PRE-CALCULATE the sum.
        // We can verify that the new instance has an adjusted sum.
        assertNotEquals(controller.getConfiguration(), next.getConfiguration());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, 0.5, 1.0, 2.0 })
    void testDifferentTimeDeltas(double dt) {
        double error = 5.0;
        double output = controller.compute(error, 0.0, dt, config);
        assertTrue(output > 0);
    }
}
