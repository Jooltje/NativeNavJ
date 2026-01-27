package com.nativenavj.domain;

import com.nativenavj.control.Loop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Knowledge Source for user interaction via CLI.
 */
public class Shell extends Loop {
    private static final Logger log = LoggerFactory.getLogger(Shell.class);

    private final Memory memory;
    private final BufferedReader reader;
    private boolean llmEnabled;

    public Shell(Memory memory, InputStream input) {
        super(1.0); // Runs at 1Hz
        this.memory = memory;
        this.reader = new BufferedReader(new InputStreamReader(input));
        this.llmEnabled = false;
    }

    @Override
    protected void step() {
        try {
            if (reader.ready()) {
                String line = reader.readLine();
                if (line != null && !line.isBlank()) {
                    execute(line.trim().toUpperCase());
                }
            }
        } catch (Exception e) {
            log.error("Error in Shell step", e);
        }
    }

    public String execute(String command) {
        if (command == null)
            return "ERROR: Command is null";
        command = command.trim().toUpperCase();
        log.info("User Command: {}", command);

        try {
            if (command.startsWith("SYS")) {
                return executeSys(command.substring(3).trim());
            } else if (command.startsWith("HDG")) {
                return executeHdg(command.substring(3).trim());
            } else if (command.startsWith("ALT")) {
                return executeAlt(command.substring(3).trim());
            } else if (command.startsWith("SPD")) {
                return executeSpd(command.substring(3).trim());
            } else if (command.startsWith("LLM")) {
                return executeLlm(command.substring(3).trim());
            } else if (command.startsWith("ASK")) {
                return executeAsk(command.substring(3).trim());
            } else {
                log.warn("Unknown command: {}", command);
                return "ERROR: Unknown command";
            }
        } catch (Exception e) {
            log.error("Error executing command: {}", command, e);
            return "ERROR: " + e.getMessage();
        }
    }

    private String executeSys(String arg) {
        if ("ON".equals(arg)) {
            memory.setNavigator(Navigator.active("AUTONOMOUS"));
            log.info("System ON");
            return "System enabled";
        } else if ("OFF".equals(arg)) {
            memory.setNavigator(Navigator.inactive());
            log.info("System OFF");
            return "System disabled";
        }
        return "ERROR: Invalid SYS argument";
    }

    private String executeHdg(String arg) {
        if (arg.isEmpty())
            return "ERROR: Missing argument";
        double hdg = Double.parseDouble(arg);
        // Normalize heading to [0, 360)
        hdg = ((hdg % 360) + 360) % 360;
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(goal.getAltitude(), goal.getSpeed(), hdg));
        return "Heading set to " + hdg;
    }

    private String executeAlt(String arg) {
        double alt = Double.parseDouble(arg);
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(alt, goal.getSpeed(), goal.getHeading()));
        return "Altitude set to " + alt;
    }

    private String executeSpd(String arg) {
        double spd = Double.parseDouble(arg);
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(goal.getAltitude(), spd, goal.getHeading()));
        return "Airspeed set to " + spd;
    }

    private String executeLlm(String arg) {
        if ("ON".equals(arg)) {
            llmEnabled = true;
            log.info("LLM Enabled");
            return "LLM enabled";
        } else if ("OFF".equals(arg)) {
            llmEnabled = false;
            memory.setAssistant(com.nativenavj.domain.Assistant.inactive());
            log.info("LLM Disabled");
            return "LLM disabled";
        }
        return "ERROR: Invalid LLM argument";
    }

    private String executeAsk(String prompt) {
        if (!llmEnabled) {
            log.warn("LLM is disabled. Use LLM ON first.");
            return "ERROR: LLM is disabled";
        }
        memory.setAssistant(
                new com.nativenavj.domain.Assistant(true, com.nativenavj.domain.Assistant.Status.THINKING, prompt));
        return "Assistant thinking...";
    }

    public boolean isLlmEnabled() {
        return llmEnabled;
    }

    public Memory getMemory() {
        return memory;
    }
}
