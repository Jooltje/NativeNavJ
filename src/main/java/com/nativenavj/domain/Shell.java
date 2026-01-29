package com.nativenavj.domain;

import com.nativenavj.control.Orchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Knowledge Source for user interaction via CLI.
 */
public class Shell implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Shell.class);

    private final Memory memory;
    private final BufferedReader reader;
    private Orchestrator orchestrator;
    private boolean llm;

    public Shell(Memory memory, InputStream input) {
        this.memory = memory;
        this.reader = new BufferedReader(new InputStreamReader(input));
        this.llm = false;
    }

    public void setOrchestrator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        try {
            System.out.print("> ");
            System.out.flush();
            String line = reader.readLine();
            if (line != null && !line.isBlank()) {
                String result = execute(line.trim().toUpperCase());
                System.out.println(result);
            }
        } catch (Exception e) {
            log.error("Error in Shell run", e);
        }
    }

    public String execute(String command) {
        if (command == null)
            return "ERROR: Command is null";
        command = command.trim().toUpperCase();
        log.info("User Command: {}", command);

        try {
            if (command.startsWith("SET")) {
                return executeSet(command.substring(3).trim());
            } else if (command.startsWith("SYS")) {
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

    private String executeSet(String arg) {
        String[] parts = arg.split("\\s+");
        if (parts.length < 3) {
            return "ERROR: SET requires <name> <parameter> <value>";
        }

        String name = parts[0].toUpperCase();
        String parameter = parts[1].toUpperCase();
        String valueStr = parts[2].toUpperCase();

        Configuration current = memory.getProfile(name);
        boolean isNew = current == null;
        if (isNew) {
            current = new Configuration(0, 0, 0, -1, 1);
        }

        Configuration updated = current;
        Double frequency = null;
        Boolean active = null;

        try {
            switch (parameter) {
                case "SYS" -> active = "ON".equals(valueStr) || "TRUE".equals(valueStr);
                case "FRQ" -> frequency = Double.parseDouble(valueStr);
                case "KP" -> updated = new Configuration(Double.parseDouble(valueStr), current.integral(),
                        current.derivative(), current.minimum(), current.maximum());
                case "KI" -> updated = new Configuration(current.proportion(), Double.parseDouble(valueStr),
                        current.derivative(), current.minimum(), current.maximum());
                case "KD" -> updated = new Configuration(current.proportion(), current.integral(),
                        Double.parseDouble(valueStr), current.minimum(), current.maximum());
                default -> {
                    return "ERROR: Unknown parameter: " + parameter;
                }
            }
        } catch (Exception e) {
            return "ERROR: Invalid value for " + parameter;
        }

        if (orchestrator != null) {
            orchestrator.configure(name, (updated != current || isNew) ? updated : null, frequency, active);
        } else {
            // Fallback for standalone tests if orchestrator not set
            if (updated != current || isNew)
                memory.setProfile(name, updated);
            if (frequency != null)
                memory.setFrequency(name, frequency);
            if (active != null)
                memory.setActive(name, active);
        }

        return String.format("Set %s %s to %s", name, parameter, valueStr);
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
        memory.setGoal(new Goal(goal.height(), goal.velocity(), hdg));
        return "Heading set to " + hdg;
    }

    private String executeAlt(String arg) {
        double alt = Double.parseDouble(arg);
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(alt, goal.velocity(), goal.direction()));
        return "Altitude set to " + alt;
    }

    private String executeSpd(String arg) {
        double spd = Double.parseDouble(arg);
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(goal.height(), spd, goal.direction()));
        return "Airspeed set to " + spd;
    }

    private String executeLlm(String arg) {
        if ("ON".equals(arg)) {
            llm = true;
            log.info("LLM Enabled");
            return "LLM enabled";
        } else if ("OFF".equals(arg)) {
            llm = false;
            memory.setAssistant(com.nativenavj.domain.Assistant.inactive());
            log.info("LLM Disabled");
            return "LLM disabled";
        }
        return "ERROR: Invalid LLM argument";
    }

    private String executeAsk(String prompt) {
        if (!llm) {
            log.warn("LLM is disabled. Use LLM ON first.");
            return "ERROR: LLM is disabled";
        }
        memory.setAssistant(
                new com.nativenavj.domain.Assistant(true, com.nativenavj.domain.Assistant.Status.THINKING, prompt));
        return "Assistant thinking...";
    }

    public boolean isLlm() {
        return llm;
    }

    public Memory getMemory() {
        return memory;
    }
}
