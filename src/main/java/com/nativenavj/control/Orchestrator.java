package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.ai.Assistant;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Sample;
import com.nativenavj.domain.Settings;
import com.nativenavj.domain.Shell;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates the control sources and logic sources.
 * Manages their lifecycles and execution.
 */
public class Orchestrator {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    private final Connector connector;
    private final Memory memory;
    private final Map<String, Loop> logicSources = new HashMap<>();
    private final Map<String, Loop> controlSources = new HashMap<>();
    private final Shell shell;
    private final Assistant assistant;
    private final Computer computer;

    private ScheduledExecutorService scheduler;

    public Orchestrator(Connector connector, Memory memory) {
        this.connector = connector;
        this.memory = memory;

        // Initialize Knowledge Sources
        computer = new Computer(memory);
        logicSources.put("Computer", computer);

        Settings settings = memory.getSettings();

        // Initialize Controllers with Sensor ports (using Connector data)
        controlSources.put("Pitch",
                new Pitch(connector,
                        () -> new Sample(connector.getLatestTelemetry().time(), connector.getLatestTelemetry().pitch()),
                        memory, settings.pitch()));
        controlSources.put("Roll",
                new Roll(connector,
                        () -> new Sample(connector.getLatestTelemetry().time(), connector.getLatestTelemetry().bank()),
                        memory, settings.roll()));
        controlSources.put("Yaw", new Yaw(connector,
                () -> new Sample(connector.getLatestTelemetry().time(), connector.getLatestTelemetry().heading()),
                memory, settings.yaw()));
        controlSources.put("Throttle", new Throttle(connector,
                () -> new Sample(connector.getLatestTelemetry().time(), connector.getLatestTelemetry().airspeed()),
                memory, settings.throttle()));

        shell = new Shell(memory, System.in);
        assistant = new Assistant(memory, shell);
    }

    /**
     * Starts all loops.
     */
    public void start() {
        log.info("Starting Orchestrator...");
        scheduler = Executors.newScheduledThreadPool(logicSources.size() + controlSources.size() + 2);

        // Start Logic Sources (TECS Computer, etc.)
        logicSources.forEach((name, loop) -> {
            scheduler.scheduleAtFixedRate(loop::executeStep, 0, loop.getPeriodNanos(), TimeUnit.NANOSECONDS);
            log.info("Started Logic Source: {}", name);
        });

        // Start Control Sources (PID Controllers)
        controlSources.forEach((name, loop) -> {
            scheduler.scheduleAtFixedRate(loop::executeStep, 0, loop.getPeriodNanos(), TimeUnit.NANOSECONDS);
            log.info("Started Control Source: {}", name);
        });

        // Start Interaction Sources
        scheduler.scheduleAtFixedRate(shell::executeStep, 0, shell.getPeriodNanos(), TimeUnit.NANOSECONDS);
        scheduler.scheduleAtFixedRate(assistant::executeStep, 0, assistant.getPeriodNanos(), TimeUnit.NANOSECONDS);

        log.info("Orchestrator started.");
    }

    /**
     * Stops all loops.
     */
    public void stop() {
        log.info("Stopping Orchestrator...");
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        log.info("Orchestrator stopped.");
    }
}
