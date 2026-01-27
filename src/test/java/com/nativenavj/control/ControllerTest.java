package com.nativenavj.control;

import com.nativenavj.domain.Configuration;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Target;
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

    private Memory memory;
    private Actuator actuator;
    private Sensor sensor;
    private TestController controller;

    @BeforeEach
    void setUp() {
        memory = new Memory();
        actuator = mock(Actuator.class);
        sensor = mock(Sensor.class);
        Configuration config = new Configuration(true, 50.0, 1.0, 0.1, 0.05, -100.0, 100.0);
        controller = new TestController(memory, actuator, sensor, config);
    }

    @Test
    void testProportionalResponse() {
        double error = 10.0;
        double output = controller.compute(error, 0.0, 1.0);
        assertEquals(11.0, output, 0.01);
    }

    @Test
    void testIntegralAccumulation() {
        double error = 5.0;
        double dt = 1.0;
        double output1 = controller.compute(error, 0.0, dt);
        assertEquals(5.5, output1, 0.01);
        double output2 = controller.compute(error, 0.0, dt);
        assertEquals(6.0, output2, 0.01);
    }

    @Test
    void testIntegratorWindupPrevention() {
        // Configuration in setUp already sets limits to -10, 10
        for (int i = 0; i < 100; i++) {
            controller.compute(100.0, 0.0, 1.0);
        }
        double output = controller.compute(-5.0, 0.0, 1.0);
        assertTrue(output < 10.0, "Integral should have been clamped");
    }

    @Test
    void testDerivativeOnFeedback() {
        controller.compute(10.0, 0.0, 1.0);
        double output = controller.compute(8.0, 2.0, 1.0);
        assertEquals(9.7, output, 0.01);
    }

    @Test
    void testOutputClamping() {
        // We need a controller with specific limits for this test
        Configuration config = new Configuration(true, 50.0, 1.0, 0.0, 0.0, -5.0, 5.0);
        TestController localController = new TestController(memory, actuator, sensor, config);
        double output = localController.compute(100.0, 0.0, 1.0);
        assertEquals(5.0, output, 0.01);
        output = localController.compute(-100.0, 0.0, 1.0);
        assertEquals(-5.0, output, 0.01);
    }

    @Test
    void testReset() {
        controller.compute(10.0, 0.0, 1.0);
        controller.compute(10.0, 0.0, 1.0);
        controller.reset();
        double output = controller.compute(10.0, 0.0, 1.0);
        assertEquals(11.0, output, 0.01);
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, 0.5, 1.0, 2.0 })
    void testDifferentTimeDeltas(double dt) {
        double error = 5.0;
        double output = controller.compute(error, 0.0, dt);
        assertTrue(output > 0);
    }

    private static class TestController extends Controller {
        public TestController(Memory memory, Actuator actuator, Sensor sensor, Configuration config) {
            super(memory, actuator, sensor, config);
        }

        @Override
        protected double getSetpoint(Target target) {
            return 0;
        }

        @Override
        protected void sendCommand(double output) {
        }
    }
}
