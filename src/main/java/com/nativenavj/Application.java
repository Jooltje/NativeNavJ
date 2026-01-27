package com.nativenavj;

import com.nativenavj.adapter.Connector;
import com.nativenavj.control.Computer;
import com.nativenavj.control.Orchestrator;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point for NativeNavJ.
 * Bootstraps the system by initializing all core components.
 */
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("Starting NativeNavJ Application");

        try {
            // Initialize Blackboard
            Memory memory = new Memory();

            // Initialize Adapter (SimConnect)
            Connector connector = new Connector(memory);

            // Initialize Knowledge Sources
            Computer computer = new Computer(memory);
            Shell shell = new Shell(memory, System.in);

            // Initialize Orchestrator and start periodic tasks
            Orchestrator orchestrator = new Orchestrator(memory, connector, computer, shell);
            orchestrator.start();

            log.info("Application successfully bootstrapped");

            // Add shutdown hook for graceful exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down NativeNavJ...");
                orchestrator.stop();
                connector.stop();
            }));

            // Keep main thread alive
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            log.error("Fatal error during application bootstrap", e);
            System.exit(1);
        }
    }
}
