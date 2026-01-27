package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Settings;
import com.nativenavj.domain.Sample;
import com.nativenavj.port.Objective;
import com.nativenavj.port.Actuator;
import com.nativenavj.port.Sensor;
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
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, Controller> controlSources = new HashMap<>();

    public Orchestrator(Memory memory, Connector connector) {
        this.memory = memory;
        this.connector = connector;

        initializeControllers();
    }

    private void initializeControllers() {
        Settings settings = memory.getSettings();

        // Pitch Controller
        Actuator pitchActuator = val -> connector.setElevator(val);
        Sensor pitchSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getPitch());
        Objective pitchObjective = () -> memory.getTarget().getPitch();
        Controller pitch = new Controller(pitchObjective, pitchActuator, pitchSensor, settings.getPitch());
        controlSources.put("pitch", pitch);

        // Roll Controller
        Actuator rollActuator = val -> connector.setAileron(val);
        Sensor rollSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getRoll());
        Objective rollObjective = () -> memory.getTarget().getRoll();
        Controller roll = new Controller(rollObjective, rollActuator, rollSensor, settings.getRoll());
        controlSources.put("roll", roll);

        // Yaw Controller
        Actuator yawActuator = val -> connector.setRudder(val);
        Sensor yawSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getYaw());
        Objective yawObjective = () -> memory.getTarget().getYaw();
        Controller yaw = new Controller(yawObjective, yawActuator, yawSensor, settings.getYaw());
        controlSources.put("yaw", yaw);

        // Throttle Controller
        Actuator throttleActuator = val -> connector.setThrottle(val);
        Sensor throttleSensor = () -> new Sample(memory.getState().getTime(), memory.getState().getSpeed());
        Objective throttleObjective = () -> memory.getTarget().getThrottle();
        Controller throttle = new Controller(throttleObjective, throttleActuator, throttleSensor,
                settings.getThrottle());
        controlSources.put("throttle", throttle);

        log.info("Controllers initialized");
    }

    public void start() {
        for (Controller controller : controlSources.values()) {
            long periodMicros = (long) (1_000_000.0 / controller.getConfiguration().getFrequency());
            scheduler.scheduleAtFixedRate(controller, 0, periodMicros, TimeUnit.MICROSECONDS);
        }
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
