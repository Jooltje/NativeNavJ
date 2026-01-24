package com.nativenavj.control.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericPIDTest {

    @Test
    public void testZeroErrorProducesZeroOutput() {
        GenericPID pid = new GenericPID(1.0, 0.1, 0.05, -10, 10);
        double output = pid.calculate(100, 100, 0.1);
        assertEquals(0.0, output, 0.0001);
    }

    @Test
    public void testProportionalTerm() {
        GenericPID pid = new GenericPID(2.0, 0, 0, -100, 100);
        // Error = 10, Kp = 2 -> Output = 20
        double output = pid.calculate(100, 90, 0.1);
        assertEquals(20.0, output, 0.0001);
    }

    @Test
    public void testIntegralAccumulation() {
        GenericPID pid = new GenericPID(0, 1.0, 0, -100, 100);
        // Step 1: Error = 10, dt = 0.1 -> Integral = 1, Output = 1
        double out1 = pid.calculate(100, 90, 0.1);
        assertEquals(1.0, out1, 0.0001);

        // Step 2: Error = 10, dt = 0.1 -> Integral = 2, Output = 2
        double out2 = pid.calculate(100, 90, 0.1);
        assertEquals(2.0, out2, 0.0001);
    }

    @Test
    public void testOutputClamping() {
        GenericPID pid = new GenericPID(100.0, 0, 0, -10, 10);
        // Error = 1, Kp = 100 -> Expected 100, but clamped to 10
        double output = pid.calculate(10, 9, 0.1);
        assertEquals(10.0, output, 0.0001);
    }

    @Test
    public void testDerivativeTerm() {
        GenericPID pid = new GenericPID(0, 0, 1.0, -100, 100);
        // Step 1: First run -> Derivative is 0 (prevError not yet set)
        double out1 = pid.calculate(100, 90, 0.1);
        assertEquals(0.0, out1, 0.0001);

        // Step 2: Next run. PrevError = 10, CurrentError = 5, dt = 0.1
        // -> Derivative = (5-10)/0.1 = -50
        double out2 = pid.calculate(100, 95, 0.1);
        assertEquals(-50.0, out2, 0.0001);
    }
}
