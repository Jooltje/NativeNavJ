package com.nativenavj.control;

import com.nativenavj.domain.Command;
import com.nativenavj.domain.Goal;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Status;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;
import com.nativenavj.port.Sensor;

/**
 * Total Energy Control System (TECS) Computer.
 * Manages energy balance (throttle) and energy distribution (pitch).
 * Coordinates individual PID controllers for each control surface.
 */
public class Computer {
    private static final double GRAVITY_FT_PER_S2 = 32.174;
    private static final double KNOTS_TO_FT_PER_S = 1.68781;
    private static final double MIN_STALL_KTS = 40.0;

    // TECS parameters
    private static final double TECS_SPDWEIGHT = 1.0; // Balance between altitude and speed priority
    private static final double TECS_TIME_CONST = 5.0; // Responsiveness (seconds)

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    private final PitchController pitchController;
    private final RollController rollController;
    private final YawController yawController;
    private final ThrottleController throttleController;

    private long lastComputeNanos = 0;

    public Computer(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;

        this.pitchController = new PitchController(clock);
        this.rollController = new RollController(clock);
        this.yawController = new YawController(clock);
        this.throttleController = new ThrottleController(clock);
    }

    /**
     * Main computation loop for TECS.
     * Reads state, computes control commands, and writes to actuator.
     */
    public void compute(Goal goal, Status status, double dt) {
        if (!status.active() || !sensor.isAvailable() || !actuator.isReady()) {
            return;
        }

        State state = sensor.read();

        // Stall protection - highest priority
        if (state.airspeedKts() < MIN_STALL_KTS) {
            Command stallRecovery = new Command(-10.0, 0.0, 1.0, 0.0);
            actuator.write(stallRecovery);
            return;
        }

        // Calculate energy errors
        double currentEnergy = calculateSpecificEnergy(state.altitudeFt(), state.airspeedKts());
        double targetEnergy = calculateSpecificEnergy(goal.targetAltitudeFt(), goal.targetAirspeedKts());
        double energyError = targetEnergy - currentEnergy;

        // Calculate energy distribution error
        double altitudeError = goal.targetAltitudeFt() - state.altitudeFt();
        double speedError = goal.targetAirspeedKts() - state.airspeedKts();
        double distributionError = altitudeError - (TECS_SPDWEIGHT * speedError);

        // Throttle control (total energy)
        double throttle = throttleController.computeThrottleCommand(
                energyError / 1000.0, // Normalize
                0.5, // Assume mid-throttle as feedback
                dt);

        // Pitch control (energy distribution)
        double targetPitch = distributionError * 0.01; // Simple gain
        double pitch = pitchController.computePitchCommand(
                targetPitch,
                state.pitchDeg(),
                dt);

        // Roll control (heading tracking)
        double roll = rollController.computeRollCommand(
                goal.targetHeadingDeg(),
                state.headingDeg(),
                dt);

        // Yaw control (coordination)
        double rudder = yawController.computeRudderCommand(
                state.rollDeg(),
                0.0, // Would need yaw rate from state
                dt);

        Command command = new Command(pitch, roll, throttle, rudder).clamp();
        actuator.write(command);
    }

    /**
     * Calculates specific energy: E_s = h + V²/2g
     */
    public double calculateSpecificEnergy(double altitudeFt, double airspeedKts) {
        double velocityFtPerS = airspeedKts * KNOTS_TO_FT_PER_S;
        double kineticEnergy = (velocityFtPerS * velocityFtPerS) / (2.0 * GRAVITY_FT_PER_S2);
        return altitudeFt + kineticEnergy;
    }

    /**
     * Calculates specific energy rate: dE_s/dt ≈ γ + dV/g
     */
    public double calculateEnergyRate(State state) {
        // Flight path angle approximation: γ ≈ VS / V
        double velocityFtPerS = state.airspeedKts() * KNOTS_TO_FT_PER_S;
        double verticalSpeedFtPerS = state.verticalSpeedFpm() / 60.0;

        if (velocityFtPerS < 1.0)
            return 0.0;

        double gamma = verticalSpeedFtPerS / velocityFtPerS;

        // For steady state, assume dV/dt ≈ 0
        // Full implementation would track velocity changes
        return gamma;
    }

    /**
     * Calculates energy distribution rate: dD_s/dt ≈ γ - dV/g
     */
    public double calculateEnergyDistribution(State state) {
        double velocityFtPerS = state.airspeedKts() * KNOTS_TO_FT_PER_S;
        double verticalSpeedFtPerS = state.verticalSpeedFpm() / 60.0;

        if (velocityFtPerS < 1.0)
            return 0.0;

        double gamma = verticalSpeedFtPerS / velocityFtPerS;

        // For steady state, assume dV/dt ≈ 0
        return gamma;
    }

    /**
     * Resets all controllers.
     */
    public void reset() {
        pitchController.reset();
        rollController.reset();
        yawController.reset();
        throttleController.reset();
    }
}
