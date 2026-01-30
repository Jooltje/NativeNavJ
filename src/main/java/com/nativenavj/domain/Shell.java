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
 
    public Shell(Memory memory, InputStream input) {
        this.memory = memory;
        this.reader = new BufferedReader(new InputStreamReader(input));
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
                return executeSystem(command.substring(3).trim());
            } else if (command.startsWith("HDG")) {
                return executeHeading(command.substring(3).trim());
            } else if (command.startsWith("ALT")) {
                return executeAltitude(command.substring(3).trim());
            } else if (command.startsWith("SPD")) {
                return executeSpeed(command.substring(3).trim());
            } else if (command.startsWith("LLM")) {
                return executeAssistant(command.substring(3).trim());
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
        String[] components = arg.split("\\s+");
        if (components.length < 3) {
            return "ERROR: SET requires <name> <parameter> <value>";
        }

        String name = components[0].toUpperCase();
        String parameter = components[1].toUpperCase();
        String text = components[2].toUpperCase();

        // Map CLI codes to full names
        String registryKey = switch (name) {
            case "CPU" -> "COMPUTER";
            case "SHL" -> "SHELL";
            case "ORC", "JOB" -> "ORCHESTRATOR";
            case "INP" -> "SHELL";
            case "PIT" -> "PITCH";
            case "ROL" -> "ROLL";
            case "THR" -> "THROTTLE";
            case "YAW" -> "YAW";
            case "SPD" -> "SPEED";
            case "ALT" -> "ALTITUDE";
            case "HDG" -> "HEADING";
            case "LLM" -> "ASSISTANT";
            default -> name;
        };

        Configuration current = memory.getProfile(registryKey);
        boolean isNew = current == null;
        if (isNew) {
            current = new Configuration(0, 0, 0, -1, 1);
        }

        Configuration updated = current;
        Double frequency = null;
        Boolean active = null;

        try {
            switch (parameter) {
                case "SYS" -> active = "ON".equals(text) || "TRUE".equals(text);
                case "FRQ" -> frequency = Double.parseDouble(text);
                case "KP" -> updated = new Configuration(Double.parseDouble(text), current.integral(),
                        current.derivative(), current.minimum(), current.maximum());
                case "KI" -> updated = new Configuration(current.proportion(), Double.parseDouble(text),
                        current.derivative(), current.minimum(), current.maximum());
                case "KD" -> updated = new Configuration(current.proportion(), current.integral(),
                        Double.parseDouble(text), current.minimum(), current.maximum());
                default -> {
                    return "ERROR: Unknown parameter: " + parameter;
                }
            }
        } catch (Exception e) {
            return "ERROR: Invalid value for " + parameter;
        }

        if (orchestrator != null) {
            orchestrator.configure(registryKey, (updated != current || isNew) ? updated : null, frequency, active);
        } else {
            // Fallback for standalone tests if orchestrator not set
            if (updated != current || isNew)
                memory.setProfile(registryKey, updated);
            if (frequency != null)
                memory.setFrequency(registryKey, frequency);
            if (active != null)
                memory.setActive(registryKey, active);
        }

        return String.format("Set %s %s to %s", registryKey, parameter, text);
    }

    private String executeSystem(String argument) {
        boolean active = "ON".equals(argument);
        String[] components = { "COMPUTER", "PITCH", "ROLL", "YAW", "THROTTLE", "ASSISTANT" };

        if (active) {
            memory.setNavigator(Navigator.active("AUTONOMOUS"));
            for (String component : components) {
                if (orchestrator != null) {
                    orchestrator.configure(component, null, null, true);
                } else {
                    memory.setActive(component, true);
                }
            }
            log.info("System ON");
            return "All systems enabled";
        } else if ("OFF".equals(argument)) {
            memory.setNavigator(Navigator.inactive());
            for (String component : components) {
                if (orchestrator != null) {
                    orchestrator.configure(component, null, null, false);
                } else {
                    memory.setActive(component, false);
                }
            }
            log.info("System OFF");
            return "All systems disabled";
        }
        return "ERROR: Invalid SYS argument";
    }

    private String executeHeading(String argument) {
        if (argument.isEmpty())
            return "ERROR: Missing argument";
        double heading = Double.parseDouble(argument);
        // Normalize heading to [0, 360)
        heading = ((heading % 360) + 360) % 360;
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(goal.height(), goal.velocity(), heading));
        return "Heading set to " + heading;
    }

    private String executeAltitude(String argument) {
        if (argument.isEmpty())
            return "ERROR: Missing argument";
        double altitude = Double.parseDouble(argument);
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(altitude, goal.velocity(), goal.direction()));
        return "Altitude set to " + altitude;
    }

    private String executeSpeed(String argument) {
        if (argument.isEmpty())
            return "ERROR: Missing argument";
        double speed = Double.parseDouble(argument);
        Goal goal = memory.getGoal();
        memory.setGoal(new Goal(goal.height(), speed, goal.direction()));
        return "Airspeed set to " + speed;
    }

    private String executeAssistant(String argument) {
        if ("ON".equals(argument)) {
            if (orchestrator != null) {
                orchestrator.configure("ASSISTANT", null, null, true);
            } else {
                memory.setActive("ASSISTANT", true);
            }
            log.info("Assistant Enabled");
            return "Assistant enabled";
        } else if ("OFF".equals(argument)) {
            if (orchestrator != null) {
                orchestrator.configure("ASSISTANT", null, null, false);
            } else {
                memory.setActive("ASSISTANT", false);
            }
            memory.setAssistant(com.nativenavj.domain.Assistant.inactive());
            log.info("Assistant Disabled");
            return "Assistant disabled";
        }
        return "ERROR: Invalid LLM argument";
    }

    private String executeAsk(String prompt) {
        if (!memory.isActive("ASSISTANT")) {
            log.warn("Assistant is disabled. Use LLM ON first.");
            return "ERROR: Assistant is disabled";
        }
        memory.setAssistant(
                new com.nativenavj.domain.Assistant(true, com.nativenavj.domain.Assistant.Status.THINKING, prompt));
        return "Assistant thinking...";
    }

    public boolean isAssistant() {
        return memory.isActive("ASSISTANT");
    }

    public Memory getMemory() {
        return memory;
    }
}
