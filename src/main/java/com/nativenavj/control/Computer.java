package com.nativenavj.control;

import com.nativenavj.domain.Goal;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Navigator;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Knowledge Source for TECS (Total Energy Control System) logic.
 * Calculates targets for individual controllers.
 */
public class Computer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Computer.class);

    private static final double STALL = 60.0; // kts
    private static final double WEIGHT = 1.0; // Energy distribution weight
    private static final double RATIO = 1.68781; // kts to ft/s

    private final Memory memory;

    public Computer(Memory memory) {
        this.memory = memory;
    }

    public void setAltitude(double altitude) {
        Goal current = memory.getGoal();
        memory.setGoal(new Goal(altitude, current.velocity(), current.direction()));
    }

    public void setSpeed(double speed) {
        Goal current = memory.getGoal();
        memory.setGoal(new Goal(current.height(), speed, current.direction()));
    }

    public void setHeading(double heading) {
        Goal current = memory.getGoal();
        memory.setGoal(new Goal(current.height(), current.velocity(), heading));
    }

    public void activate() {
        memory.setNavigator(Navigator.active("AUTONOMOUS"));
        log.info("Computer Activated");
    }

    public void deactivate() {
        memory.setNavigator(Navigator.inactive());
        log.info("Computer Deactivated");
    }

    @Override
    public void run() {
        if (memory.getNavigator().status()) {
            State state = memory.getState();
            Goal goal = memory.getGoal();

            // Stall protection - highest priority
            if (state.speed() < STALL) {
                memory.setTarget(new Target(0.0, -10.0, 0.0, 1.0));
                return;
            }

            // Calculate energy distribution error
            double altitudeError = goal.height() - state.altitude();
            double speedError = goal.velocity() - state.speed();

            // Energy distribution balance
            double distributionError = altitudeError - (WEIGHT * speedError);

            // Calculate specific energy error (total energy)
            double currentEnergy = calculateSpecificEnergy(state.altitude(), state.speed());
            double targetEnergy = calculateSpecificEnergy(goal.height(), goal.velocity());
            double energyError = targetEnergy - currentEnergy;

            // Generate Target outputs for Controllers
            double targetPitch = distributionError * 0.01; // Simple gain for target pitch
            double targetRoll = calculateTargetRoll(goal.direction(), state.heading());
            double targetYaw = 0.0; // Coordination handled by Controllers
            double targetPower = 0.5 + (energyError / 2000.0); // Simple bias + gain

            Target target = new Target(
                    clamp(targetRoll, -30, 30),
                    clamp(targetPitch, -15, 15),
                    targetYaw,
                    clamp(targetPower, 0.0, 1.0));

            memory.setTarget(target);
        }
    }

    private double calculateTargetRoll(double targetHdg, double currentHdg) {
        double diff = targetHdg - currentHdg;
        while (diff > 180)
            diff -= 360;
        while (diff < -180)
            diff += 360;
        return diff * 2.0; // Roll gain
    }

    public double calculateSpecificEnergy(double altitude, double speed) {
        double vFs = speed * RATIO;
        return altitude + (vFs * vFs) / (2 * 32.174);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public double calculateEnergyRate(State state) {
        double velocityFtPerS = state.speed() * RATIO;
        double verticalSpeedFtPerS = state.climb() / 60.0;
        if (velocityFtPerS < 1.0)
            return 0.0;
        return verticalSpeedFtPerS / velocityFtPerS;
    }

    public double calculateEnergyDistribution(State state) {
        double velocityFtPerS = state.speed() * RATIO;
        double verticalSpeedFtPerS = state.climb() / 60.0;
        if (velocityFtPerS < 1.0)
            return 0.0;
        return verticalSpeedFtPerS / velocityFtPerS;
    }
}
