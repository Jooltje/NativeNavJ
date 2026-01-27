package com.nativenavj.control;

import com.nativenavj.domain.Goal;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Total Energy Control System (TECS) Computer Knowledge Source.
 * Manages energy balance and distribution to generate intermediate targets.
 */
public class Computer extends Loop {
    private static final Logger log = LoggerFactory.getLogger(Computer.class);

    private static final double GRAVITY_FT_PER_S2 = 32.174;
    private static final double KNOTS_TO_FT_PER_S = 1.68781;
    private static final double MIN_STALL_KTS = 40.0;

    // TECS parameters
    private static final double TECS_SPDWEIGHT = 1.0;

    private final Memory memory;

    public Computer(Memory memory) {
        super(10.0); // Run at 10Hz as per specification
        this.memory = memory;
    }

    /**
     * Sets the target altitude via Blackboard.
     */
    public void setAltitude(double altitude) {
        Goal current = memory.getGoal();
        memory.setGoal(new Goal(altitude, current.getSpeed(), current.getHeading()));
    }

    /**
     * Sets the target airspeed via Blackboard.
     */
    public void setSpeed(double speed) {
        Goal current = memory.getGoal();
        memory.setGoal(new Goal(current.getAltitude(), speed, current.getHeading()));
    }

    /**
     * Sets the target heading via Blackboard.
     */
    public void setHeading(double heading) {
        Goal current = memory.getGoal();
        memory.setGoal(new Goal(current.getAltitude(), current.getSpeed(), heading));
    }

    /**
     * Activates autonomous control.
     */
    public void activate() {
        memory.setNavigator(com.nativenavj.domain.Navigator.active("AUTONOMOUS"));
    }

    /**
     * Deactivates autonomous control.
     */
    public void deactivate() {
        memory.setNavigator(com.nativenavj.domain.Navigator.inactive());
    }

    public Goal getGoal() {
        return memory.getGoal();
    }

    public com.nativenavj.domain.Navigator getNavigator() {
        return memory.getNavigator();
    }

    @Override
    protected void step() {
        State state = memory.getState();
        Goal goal = memory.getGoal();

        // Stall protection - highest priority
        if (state.getSpeed() < MIN_STALL_KTS) {
            memory.setTarget(new Target(0.0, -10.0, 0.0, 1.0));
            return;
        }

        // Calculate energy distribution error
        double altitudeError = goal.getAltitude() - state.getAltitude();
        double speedError = goal.getSpeed() - state.getSpeed();

        // Energy distribution balance
        double distributionError = altitudeError - (TECS_SPDWEIGHT * speedError);

        // Calculate specific energy error (total energy)
        double currentEnergy = calculateSpecificEnergy(state.getAltitude(), state.getSpeed());
        double targetEnergy = calculateSpecificEnergy(goal.getAltitude(), goal.getSpeed());
        double energyError = targetEnergy - currentEnergy;

        // Generate Target outputs for Controllers
        double targetPitch = distributionError * 0.01; // Simple gain for target pitch
        double targetRoll = calculateTargetRoll(goal.getHeading(), state.getHeading());
        double targetYaw = 0.0; // Coordination handled by Controllers
        double targetThrottle = 0.5 + (energyError / 2000.0); // Simple bias + gain

        Target target = new Target(
                clamp(targetRoll, -30, 30),
                clamp(targetPitch, -15, 15),
                targetYaw,
                clamp(targetThrottle, 0.0, 1.0));

        memory.setTarget(target);
    }

    private double calculateTargetRoll(double targetHeading, double currentHeading) {
        double error = targetHeading - currentHeading;
        while (error > 180)
            error -= 360;
        while (error < -180)
            error += 360;
        return error * 1.5; // Gain for bank angle
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public double calculateSpecificEnergy(double altitude, double speed) {
        double velocityFtPerS = speed * KNOTS_TO_FT_PER_S;
        double kineticEnergy = (velocityFtPerS * velocityFtPerS) / (2.0 * GRAVITY_FT_PER_S2);
        return altitude + kineticEnergy;
    }

    public double calculateEnergyRate(State state) {
        double velocityFtPerS = state.getSpeed() * KNOTS_TO_FT_PER_S;
        double verticalSpeedFtPerS = state.getClimb() / 60.0;
        if (velocityFtPerS < 1.0)
            return 0.0;
        return verticalSpeedFtPerS / velocityFtPerS;
    }

    public double calculateEnergyDistribution(State state) {
        double velocityFtPerS = state.getSpeed() * KNOTS_TO_FT_PER_S;
        double verticalSpeedFtPerS = state.getClimb() / 60.0;
        if (velocityFtPerS < 1.0)
            return 0.0;
        return verticalSpeedFtPerS / velocityFtPerS;
    }
}
