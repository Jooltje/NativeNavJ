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

/**
 * Central coordinator of the application.
 * Manages the lifecycle of Knowledge Sources and handles user interaction.
 */
public class Orchestrator {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    private final Memory memory = new Memory();
    private final Connector connector = new Connector();
    private final SystemClock clock = new SystemClock();

    private final Map<String, Loop> logicSources = new HashMap<>();
    private final Map<String, Loop> controlSources = new HashMap<>();

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
        new Thread(logicSources.get("Sensor"), "KS-Sensor").start();
        new Thread(assistant, "KS-Assistant").start();

        running = true;
        new Thread(this::navigationMonitor, "Orchestrator-Monitor").start();
    }

    private void navigationMonitor() {
        boolean lastActive = false;

        while (running) {
            boolean currentActive = memory.getNavigator().active();

            if (currentActive && !lastActive) {
                log.info("Navigator activated. Starting control loops.");
                startControlLoops();
            } else if (!currentActive && lastActive) {
                log.info("Navigator deactivated. Stopping control loops.");
                stopControlLoops();
            }

            lastActive = currentActive;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void startControlLoops() {
        new Thread(logicSources.get("Computer"), "KS-Computer").start();
        controlSources.forEach((name, loop) -> new Thread(loop, "KS-" + name).start());
    }

    private void stopControlLoops() {
        logicSources.get("Computer").stop();
        controlSources.values().forEach(Loop::stop);
    }

    public void run() {
        System.out.println("\n===============================================");
        System.out.println("   NativeNavJ - Orchestrator v3");
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
        logicSources.values().forEach(Loop::stop);
        controlSources.values().forEach(Loop::stop);
        assistant.stop();
        connector.shutdown();
    }

    public static void main(String[] args) {
        Orchestrator orchestrator = new Orchestrator();
        orchestrator.init();
        orchestrator.run();
    }
}
