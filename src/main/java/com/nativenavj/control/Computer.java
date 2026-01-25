package com.nativenavj.control;

import com.nativenavj.domain.Command;
import com.nativenavj.domain.Goal;
import com.nativenavj.domain.Navigator;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Memory;
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
    private final Memory memory;

    private final Pitch pitch;
    private final Roll roll;
    private final Yaw yaw;
    private final Throttle throttle;

    public Computer(Sensor sensor, Actuator actuator, Clock clock, Memory memory) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        this.memory = memory;

        this.pitch = new Pitch(clock);
        this.roll = new Roll(clock);
        this.yaw = new Yaw(clock);
        this.throttle = new Throttle(clock);
    }

    /**
     * Sets the target altitude.
     */
    public void setAltitude(double altitude) {
        Goal current = memory.goal();
        memory.updateGoal(new Goal(altitude, current.speed(), current.heading()));
    }

    /**
     * Sets the target airspeed.
     */
    public void setSpeed(double speed) {
        Goal current = memory.goal();
        memory.updateGoal(new Goal(current.altitude(), speed, current.heading()));
    }

    /**
     * Sets the target heading.
     */
    public void setHeading(double heading) {
        Goal current = memory.goal();
        memory.updateGoal(new Goal(current.altitude(), current.speed(), heading));
    }

    /**
     * Activates autonomous control.
     */
    public void activate() {
        memory.updateNavigator(Navigator.active("AUTONOMOUS"));
    }

    /**
     * Deactivates autonomous control.
     */
    public void deactivate() {
        memory.updateNavigator(Navigator.inactive());
    }

    /**
     * Gets the current goal.
     */
    public Goal getGoal() {
        return memory.goal();
    }

    /**
     * Gets the current status.
     */
    public Navigator getNavigator() {
        return memory.navigator();
    }

    /**
     * Main computation loop for TECS.
     * Reads state, computes control commands, and writes to actuator.
     */
    public void compute(double dt) {
        if (!memory.navigator().active() || !sensor.isAvailable() || !actuator.isReady()) {
            return;
        }

        State state = sensor.read();
        memory.updateState(state);
        Goal goal = memory.goal();

        // Stall protection - highest priority
        if (state.speed() < MIN_STALL_KTS) {
            Command stallRecovery = new Command(-10.0, 0.0, 1.0, 0.0);
            actuator.write(stallRecovery);
            return;
        }

        // Calculate energy errors
        double currentEnergy = calculateSpecificEnergy(state.altitude(), state.speed());
        double targetEnergy = calculateSpecificEnergy(goal.altitude(), goal.speed());
        double energyError = targetEnergy - currentEnergy;

        // Calculate energy distribution error
        double altitudeError = goal.altitude() - state.altitude();
        double speedError = goal.speed() - state.speed();
        double distributionError = altitudeError - (TECS_SPDWEIGHT * speedError);

        // Throttle control (total energy)
        double throttleCmd = this.throttle.compute(
                energyError / 1000.0, // Normalize
                0.5, // Assume mid-throttle as feedback
                dt);

        // Pitch control (energy distribution)
        double targetPitch = distributionError * 0.01; // Simple gain
        double pitchCmd = this.pitch.compute(
                targetPitch,
                state.pitch(),
                dt);

        // Roll control (heading tracking)
        double rollCmd = this.roll.compute(
                goal.heading(),
                state.heading(),
                dt);

        // Yaw control (coordination)
        double rudderCmd = this.yaw.compute(
                state.roll(),
                0.0, // Would need yaw rate from state
                dt);

        Command command = new Command(pitchCmd, rollCmd, throttleCmd, rudderCmd).clamp();
        actuator.write(command);
    }

    /**
     * Calculates specific energy: E_s = h + V²/2g
     */
    public double calculateSpecificEnergy(double altitude, double speed) {
        double velocityFtPerS = speed * KNOTS_TO_FT_PER_S;
        double kineticEnergy = (velocityFtPerS * velocityFtPerS) / (2.0 * GRAVITY_FT_PER_S2);
        return altitude + kineticEnergy;
    }

    /**
     * Calculates specific energy rate: dE_s/dt ≈ γ + dV/g
     */
    public double calculateEnergyRate(State state) {
        // Flight path angle approximation: γ ≈ VS / V
        double velocityFtPerS = state.speed() * KNOTS_TO_FT_PER_S;
        double verticalSpeedFtPerS = state.climb() / 60.0;

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
        double velocityFtPerS = state.speed() * KNOTS_TO_FT_PER_S;
        double verticalSpeedFtPerS = state.climb() / 60.0;

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
        pitch.reset();
        roll.reset();
        yaw.reset();
        throttle.reset();
    }
}
