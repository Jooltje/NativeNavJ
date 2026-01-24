package com.nativenavj.control;

import com.nativenavj.adapter.MockClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for Controller base class.
 * Tests drive the design of PID controller functionality.
 */
class ControllerTest {

    private MockClock clock;
    private TestController controller;

    @BeforeEach
    void setUp() {
        clock = new MockClock();
        controller = new TestController(1.0, 0.1, 0.05, clock);
    }

    @Test
    void testProportionalResponse() {
        // RED: Test that proportional term responds to error
        double error = 10.0;
        double output = controller.compute(error, 0.0, 1.0);

        // With Kp=1.0, Ki=0.1, dt=1.0: P=10.0, I=1.0 (10.0 * 0.1 * 1.0), D=0
        assertEquals(11.0, output, 0.01);
    }

    @Test
    void testIntegralAccumulation() {
        // RED: Test that integral term accumulates over time
        double error = 5.0;
        double dt = 1.0;

        // First call: P=5.0, I=0.5 (5.0 * 0.1 * 1.0), D=0
        double output1 = controller.compute(error, 0.0, dt);
        assertEquals(5.5, output1, 0.01);

        // Second call: P=5.0, I=1.0 (accumulated), D=0
        double output2 = controller.compute(error, 0.0, dt);
        assertEquals(6.0, output2, 0.01);
    }

    @Test
    void testIntegratorWindupPrevention() {
        // RED: Test that integral doesn't accumulate beyond limits
        controller.setOutputLimits(-10.0, 10.0);

        // Drive output to saturation
        for (int i = 0; i < 100; i++) {
            controller.compute(100.0, 0.0, 1.0);
        }

        // Now apply opposite error - output should respond quickly
        // (integral shouldn't have wound up excessively)
        double output = controller.compute(-5.0, 0.0, 1.0);
        assertTrue(output < 10.0, "Integral should have been clamped");
    }

    @Test
    void testDerivativeOnFeedback() {
        // RED: Test that derivative is calculated on feedback, not error
        // This prevents "derivative kick" on setpoint changes

        double error1 = 10.0;
        double feedback1 = 0.0;
        controller.compute(error1, feedback1, 1.0);

        // Change feedback (simulating aircraft response)
        double error2 = 8.0;
        double feedback2 = 2.0;
        double output = controller.compute(error2, feedback2, 1.0);

        // Derivative should be based on feedback change: (2.0 - 0.0) / 1.0 = 2.0
        // D term = -Kd * derivative = -0.05 * 2.0 = -0.1
        // P term = 8.0, I term = 1.0 + 0.8 = 1.8
        // Total = 8.0 + 1.8 - 0.1 = 9.7
        assertEquals(9.7, output, 0.01);
    }

    @Test
    void testOutputClamping() {
        // RED: Test that output is clamped to specified limits
        controller.setOutputLimits(-5.0, 5.0);

        double output = controller.compute(100.0, 0.0, 1.0);
        assertEquals(5.0, output, 0.01);

        output = controller.compute(-100.0, 0.0, 1.0);
        assertEquals(-5.0, output, 0.01);
    }

    @Test
    void testReset() {
        // RED: Test that reset clears integral accumulator
        controller.compute(10.0, 0.0, 1.0);
        controller.compute(10.0, 0.0, 1.0);

        controller.reset();

        // After reset, integral should be zero
        double output = controller.compute(10.0, 0.0, 1.0);
        assertEquals(11.0, output, 0.01); // P=10.0, I=1.0, D=0
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, 0.5, 1.0, 2.0 })
    void testDifferentTimeDeltas(double dt) {
        // RED: Test that controller works correctly with varying time deltas
        double error = 5.0;
        double output = controller.compute(error, 0.0, dt);

        // Output should scale appropriately with dt
        assertTrue(output > 0);
    }

    /**
     * Concrete test implementation of Controller.
     */
    private static class TestController extends Controller {
        public TestController(double kp, double ki, double kd, com.nativenavj.port.Clock clock) {
            super(kp, ki, kd, clock);
        }
    }
}
