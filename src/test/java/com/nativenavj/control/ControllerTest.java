package com.nativenavj.control;

import com.nativenavj.adapter.MockClock;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
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

    private MockClock clock;
    private Memory memory;
    private Actuator actuator;
    private TestController controller;

    @BeforeEach
    void setUp() {
        clock = new MockClock();
        memory = new Memory();
        actuator = mock(Actuator.class);
        controller = new TestController(50.0, memory, actuator, 1.0, 0.1, 0.05, clock);
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
        controller.setOutputLimits(-10.0, 10.0);
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
        controller.setOutputLimits(-5.0, 5.0);
        double output = controller.compute(100.0, 0.0, 1.0);
        assertEquals(5.0, output, 0.01);
        output = controller.compute(-100.0, 0.0, 1.0);
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
        public TestController(double hz, Memory memory, Actuator actuator, double kp, double ki, double kd,
                com.nativenavj.port.Clock clock) {
            super(hz, memory, actuator, kp, ki, kd, clock);
        }

        @Override
        protected double getSetpoint(Target target) {
            return 0;
        }

        @Override
        protected double getFeedback(State state) {
            return 0;
        }

        @Override
        protected void sendCommand(double output) {
        }
    }
}
