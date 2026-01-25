package com.nativenavj.control;

import com.nativenavj.adapter.Connector;
import com.nativenavj.adapter.SystemClock;
import com.nativenavj.ai.Assistant;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Central coordinator of the application.
 * Manages the lifecycle of Knowledge Sources using a ScheduledExecutorService.
 */
public class Orchestrator {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    private final Memory memory = new Memory();
    private final Connector connector = new Connector();
    private final SystemClock clock = new SystemClock();

    private final Map<String, Loop> logicSources = new HashMap<>();
    private final Map<String, Loop> controlSources = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    private Shell shell;
    private Assistant assistant;
    private volatile boolean running = false;

    public void init() {
        log.info("Initializing NativeNavJ Orchestrator...");

        connector.connect();

        // Initialize Knowledge Sources
        logicSources.put("Sensor", new Sensor(connector, memory, clock));
        logicSources.put("Computer", new Computer(memory, clock));

        controlSources.put("Pitch", new Pitch(connector, memory, clock));
        controlSources.put("Roll", new Roll(connector, memory, clock));
        controlSources.put("Yaw", new Yaw(connector, memory, clock));
        controlSources.put("Throttle", new Throttle(connector, memory, clock));

        shell = new Shell((Computer) logicSources.get("Computer"));
        assistant = new Assistant(shell, memory, clock);

        log.info("System initialized. Starting base loops...");
        startBaseLoops();
    }

    private void startBaseLoops() {
        scheduleLoop("Sensor", logicSources.get("Sensor"));
        scheduleLoop("Assistant", assistant);

        running = true;
        scheduler.scheduleAtFixedRate(this::navigationMonitor, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void scheduleLoop(String name, Loop loop) {
        if (futures.containsKey(name)) {
            return;
        }
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                loop::executeStep,
                0,
                loop.getPeriodNanos(),
                TimeUnit.NANOSECONDS);
        futures.put(name, future);
        log.debug("Scheduled loop: {}", name);
    }

    private void cancelLoop(String name) {
        ScheduledFuture<?> future = futures.remove(name);
        if (future != null) {
            future.cancel(false);
            log.debug("Cancelled loop: {}", name);
        }
    }

    private void navigationMonitor() {
        if (!running)
            return;

        boolean currentActive = memory.getNavigator().active();
        boolean anyControlRunning = futures.containsKey("Computer");

        if (currentActive && !anyControlRunning) {
            log.info("Navigator activated. Starting control loops.");
            startControlLoops();
        } else if (!currentActive && anyControlRunning) {
            log.info("Navigator deactivated. Stopping control loops.");
            stopControlLoops();
        }
    }

    private void startControlLoops() {
        scheduleLoop("Computer", logicSources.get("Computer"));
        controlSources.forEach(this::scheduleLoop);
    }

    private void stopControlLoops() {
        cancelLoop("Computer");
        controlSources.keySet().forEach(this::cancelLoop);
    }

    public void run() {
        System.out.println("\n===============================================");
        System.out.println("   NativeNavJ - Orchestrator v4 (Scheduled)");
        System.out.println("   Connected & Running. Type 'exit' to quit.");
        System.out.println("===============================================\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                System.out.print("COMMAND > ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                        running = false;
                    } else if (!input.isEmpty()) {
                        log.info("User Command: {}", input);
                        String response = assistant.issueCommand(input);
                        System.out.println("CO-PILOT > " + response);
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("CLI error", e);
        }

        shutdown();
    }

    public void shutdown() {
        log.info("Shutting down Orchestrator...");
        running = false;

        // Cancel all scheduled tasks
        futures.values().forEach(f -> f.cancel(false));
        futures.clear();

        // Shut down scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        connector.shutdown();
    }

    public static void main(String[] args) {
        Orchestrator orchestrator = new Orchestrator();
        orchestrator.init();
        orchestrator.run();
    }
}
