package com.nativenavj.control;

import com.nativenavj.domain.Command;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.State;
import com.nativenavj.domain.Target;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Knowledge Source responsible for low-level surface actuation.
 * Translates intermediate Targets and aircraft State into raw commands.
 */
public class Controllers extends Loop {
    private static final Logger log = LoggerFactory.getLogger(Controllers.class);

    private final Actuator actuator;
    private final Memory memory;

    private final Pitch pitchController;
    private final Roll rollController;
    private final Yaw yawController;
    private final Throttle throttleController;

    private long lastStepNanos = 0;

    public Controllers(Actuator actuator, Memory memory, Clock clock) {
        super(50.0, clock); // Run at 50Hz for smooth control
        this.actuator = actuator;
        this.memory = memory;

        this.pitchController = new Pitch(clock);
        this.rollController = new Roll(clock);
        this.yawController = new Yaw(clock);
        this.throttleController = new Throttle(clock);
    }

    @Override
    protected void step() {
        if (!actuator.isReady()) {
            return;
        }

        State state = memory.getState();
        Target target = memory.getTarget();

        long now = System.nanoTime();
        double dt = (lastStepNanos == 0) ? 1.0 / 50.0 : (now - lastStepNanos) / 1_000_000_000.0;
        lastStepNanos = now;

        // Compute individual axis commands
        double pitchCmd = pitchController.compute(target.pitch(), state.pitch(), dt);
        double rollCmd = rollController.compute(target.roll(), state.roll(), dt);
        double rudderCmd = yawController.compute(target.yaw(), state.yaw(), dt);
        double throttleCmd = throttleController.compute(target.throttle(), 0.5, dt); // Simple feedback loop

        Command command = new Command(pitchCmd, rollCmd, throttleCmd, rudderCmd).clamp();
        actuator.write(command);
    }

    public void reset() {
        pitchController.reset();
        rollController.reset();
        yawController.reset();
        throttleController.reset();
        lastStepNanos = 0;
    }
}
