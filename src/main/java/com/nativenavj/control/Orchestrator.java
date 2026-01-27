package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Settings;
import com.nativenavj.domain.Sample;
import com.nativenavj.port.Objective;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;
import com.nativenavj.domain.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates the control sources and handles their scheduling.
 */
public class Orchestrator {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    private final Memory memory;
    private final Connector connector;
    private final Computer computer;
    private final Shell shell;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    private final Map<String, Controller> controllers = new HashMap<>();

    public Orchestrator(Memory memory, Connector connector, Computer computer, Shell shell) {
        this.memory = memory;
        this.connector = connector;
        this.computer = computer;
        this.shell = shell;

        initializeControllers();
    }

    private void initializeControllers() {
        Settings settings = memory.getSettings();

        // Pitch Controller
        Actuator pitchActuator = val -> connector.setElevator(val);
        Sensor pitchSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getPitch());
        Objective pitchObjective = () -> memory.getTarget().getPitch();
        Controller pitch = new Controller(pitchObjective, pitchActuator, pitchSensor, settings.getPitch());
        controllers.put("pitch", pitch);

        // Roll Controller
        Actuator rollActuator = val -> connector.setAileron(val);
        Sensor rollSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getRoll());
        Objective rollObjective = () -> memory.getTarget().getRoll();
        Controller roll = new Controller(rollObjective, rollActuator, rollSensor, settings.getRoll());
        controllers.put("roll", roll);

        // Yaw Controller
        Actuator yawActuator = val -> connector.setRudder(val);
        Sensor yawSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getYaw());
        Objective yawObjective = () -> memory.getTarget().getYaw();
        Controller yaw = new Controller(yawObjective, yawActuator, yawSensor, settings.getYaw());
        controllers.put("yaw", yaw);

        // Throttle Controller
        Actuator throttleActuator = val -> connector.setThrottle(val);
        Sensor throttleSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getSpeed());
        Objective throttleObjective = () -> memory.getTarget().getThrottle();
        Controller throttle = new Controller(throttleObjective, throttleActuator, throttleSensor,
                settings.getThrottle());
        controllers.put("throttle", throttle);

        log.info("Controllers initialized");
    }

    public void start() {
        // Schedule Controllers
        for (Controller controller : controllers.values()) {
            long periodMicros = (long) (1_000_000.0 / controller.getConfiguration().frequency());
            scheduler.scheduleAtFixedRate(controller, 0, periodMicros, TimeUnit.MICROSECONDS);
        }

        // Schedule Computer
        scheduler.scheduleAtFixedRate(computer, 0, computer.getPeriodNanos(), TimeUnit.NANOSECONDS);

        // Schedule Shell
        scheduler.scheduleAtFixedRate(shell, 0, shell.getPeriodNanos(), TimeUnit.NANOSECONDS);

        log.info("Orchestrator started");
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Orchestrator stopped");
    }
}
