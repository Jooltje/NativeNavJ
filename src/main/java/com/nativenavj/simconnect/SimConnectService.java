package com.nativenavj.simconnect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nativenavj.adapter.Connector;
import com.nativenavj.adapter.SystemClock;
import com.nativenavj.control.Computer;
import com.nativenavj.control.Controllers;
import com.nativenavj.control.Sensor;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Shell;
import com.nativenavj.safety.SafetyGuardrails;
import com.nativenavj.strategy.CognitiveOrchestrator;
import com.nativenavj.util.LogManager;

import java.util.Scanner;

/**
 * Main entry point for the NativeNavJ system.
 * Orchestrates the Connector, Blackboard (Memory), and Knowledge Sources.
 */
public class SimConnectService {
    private static final Logger logger = LoggerFactory.getLogger(SimConnectService.class);

    private final Connector connector = new Connector();
    private final Memory memory = new Memory();
    private final SystemClock clock = new SystemClock();

    private final Sensor sensor;
    private final Computer computer;
    private final Controllers controllers;

    private Shell shell;
    private CognitiveOrchestrator cognitiveOrchestrator;
    private final SafetyGuardrails safetyGuardrails = new SafetyGuardrails();

    private volatile boolean running = false;

    public SimConnectService() {
        this.sensor = new Sensor(connector, memory, clock);
        this.computer = new Computer(memory, clock);
        this.controllers = new Controllers(connector, memory, clock);
    }

    public void init() {
        logger.info("Initializing NativeNavJ system with Blackboard Architecture...");

        // Start Connector (Project Panama)
        connector.connect();

        // Initialize Shell and AI layer
        shell = new Shell(computer);
        try {
            this.cognitiveOrchestrator = new CognitiveOrchestrator(shell, safetyGuardrails);
        } catch (Exception e) {
            LogManager.warn("Failed to initialize CognitiveOrchestrator. AI features will be disabled.");
        }

        // Start Knowledge Source thread loops
        startKnowledgeSources();
    }

    private void startKnowledgeSources() {
        running = true;
        new Thread(sensor, "KS-Sensor").start();
        new Thread(computer, "KS-Computer").start();
        new Thread(controllers, "KS-Controllers").start();
        logger.info("Knowledge Source loops started.");
    }

    public void disconnect() {
        running = false;
        sensor.stop();
        computer.stop();
        controllers.stop();
        connector.disconnect();
    }

    public void shutdown() {
        disconnect();
        connector.shutdown();
    }

    public static void main(String[] args) {
        SimConnectService service = new SimConnectService();
        service.init();

        System.out.println("\n===============================================");
        System.out.println("   NativeNavJ - Blackboard Architecture v2");
        System.out.println("   Connected & Running. Type 'exit' to quit.");
        System.out.println("===============================================\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (service.running) {
                System.out.print("COMMAND > ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                        service.running = false;
                    } else if (!input.isEmpty()) {
                        if (service.cognitiveOrchestrator != null) {
                            LogManager.info("User Command: " + input);
                            String response = service.cognitiveOrchestrator.issueCommand(input);
                            System.out.println("CO-PILOT > " + response);
                        } else {
                            LogManager.warn("AI Orchestrator not available.");
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            LogManager.error("CLI error", e);
        }

        service.shutdown();
        LogManager.info("Service main loop exited.");
        LogManager.close();
    }
}
