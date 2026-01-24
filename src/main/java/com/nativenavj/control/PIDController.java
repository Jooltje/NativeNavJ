package com.nativenavj.control;

import java.time.Duration;
import java.time.Instant;

public class PIDController {
    private final double kp;
    private final double ki;
    private final double kd;

    private double integral = 0;
    private double lastError = 0;
    private Instant lastTime;

    private final double minOutput;
    private final double maxOutput;

    public PIDController(double kp, double ki, double kd, double minOutput, double maxOutput) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.minOutput = minOutput;
        this.maxOutput = maxOutput;
        this.lastTime = Instant.now();
    }

    public double calculate(double target, double current) {
        Instant now = Instant.now();
        double dt = Duration.between(lastTime, now).toMillis() / 1000.0;
        if (dt <= 0)
            dt = 0.05; // Default to 20Hz if time hasn't moved

        double error = target - current;

        // Handle angular wraparound if necessary (e.g., heading)
        // This generic PID doesn't know if it's angular, so we might need a subclass or
        // wrapper

        integral += error * dt;
        double derivative = (error - lastError) / dt;

        double output = (kp * error) + (ki * integral) + (kd * derivative);

        // Clamp output
        output = Math.max(minOutput, Math.min(maxOutput, output));

        lastError = error;
        lastTime = now;

        return output;
    }

    public void reset() {
        integral = 0;
        lastError = 0;
        lastTime = Instant.now();
    }
}
