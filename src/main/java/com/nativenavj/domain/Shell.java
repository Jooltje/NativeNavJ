package com.nativenavj.domain;

import com.nativenavj.control.Loop;
import com.nativenavj.port.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Shell for parsing and executing user commands.
 * Knowledge Source that reads from an InputStream at 1Hz.
 */
public class Shell extends Loop {
    private static final Logger log = LoggerFactory.getLogger(Shell.class);

    private final Memory memory;
    private final BufferedReader reader;
    private boolean llmEnabled;

    public Shell(Memory memory, InputStream input, Clock clock) {
        super(1.0, clock); // 1Hz as per specification
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
                    String response = execute(line);
                    System.out.println("CO-PILOT > " + response);
                }
            }
        } catch (Exception e) {
            log.error("Error reading command: {}", e.getMessage());
        }
    }

    /**
     * Executes a command and returns a feedback message.
     */
    public String execute(String command) {
        if (command == null || command.isBlank()) {
            return "ERROR: Empty command";
        }

        // Normalize: trim and convert to uppercase
        String normalized = command.trim().toUpperCase();
        String[] parts = normalized.split("\\s+");

        if (parts.length == 0) {
            return "ERROR: Empty command";
        }

        String cmd = parts[0];

        try {
            return switch (cmd) {
                case "SYS" -> executeSys(parts);
                case "HDG" -> executeHdg(parts);
                case "ALT" -> executeAlt(parts);
                case "SPD" -> executeSpd(parts);
                case "LLM" -> executeLlm(parts);
                case "ASK" -> executeAsk(command); // Use original for case sensitivity
                default -> "ERROR: Unknown command '" + cmd + "'";
            };
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String executeSys(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: SYS requires ON or OFF argument";
        }

        return switch (parts[1]) {
            case "ON" -> {
                memory.setNavigator(Navigator.active("AUTONOMOUS"));
                yield "System enabled";
            }
            case "OFF" -> {
                memory.setNavigator(Navigator.inactive());
                yield "System disabled";
            }
            default -> "ERROR: Invalid SYS argument '" + parts[1] + "'. Use ON or OFF";
        };
    }

    private String executeHdg(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: HDG requires a heading value in degrees";
        }

        try {
            double heading = Double.parseDouble(parts[1]);
            heading = normalizeHeading(heading);
            Goal current = memory.getGoal();
            memory.setGoal(new Goal(current.altitude(), current.speed(), heading));
            return String.format("Heading set to %.1f degrees", heading);
        } catch (NumberFormatException e) {
            return "ERROR: Invalid heading value '" + parts[1] + "'";
        }
    }

    private String executeAlt(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: ALT requires an altitude value in feet";
        }

        try {
            double altitude = Double.parseDouble(parts[1]);
            Goal current = memory.getGoal();
            memory.setGoal(new Goal(altitude, current.speed(), current.heading()));
            return String.format("Altitude set to %.0f feet", altitude);
        } catch (NumberFormatException e) {
            return "ERROR: Invalid altitude value '" + parts[1] + "'";
        }
    }

    private String executeSpd(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: SPD requires an airspeed value in knots";
        }

        try {
            double speed = Double.parseDouble(parts[1]);
            Goal current = memory.getGoal();
            memory.setGoal(new Goal(current.altitude(), speed, current.heading()));
            return String.format("Airspeed set to %.0f knots", speed);
        } catch (NumberFormatException e) {
            return "ERROR: Invalid airspeed value '" + parts[1] + "'";
        }
    }

    private String executeLlm(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: LLM requires ON or OFF argument";
        }

        Assistant current = memory.getAssistant();
        return switch (parts[1]) {
            case "ON" -> {
                llmEnabled = true;
                memory.setAssistant(new Assistant(true, current.status(), current.prompt()));
                yield "LLM control enabled";
            }
            case "OFF" -> {
                llmEnabled = false;
                memory.setAssistant(new Assistant(false, current.status(), current.prompt()));
                yield "LLM control disabled";
            }
            default -> "ERROR: Invalid LLM argument '" + parts[1] + "'. Use ON or OFF";
        };
    }

    private String executeAsk(String command) {
        // Extract prompt after "ASK "
        String upperCommand = command.toUpperCase();
        int askIndex = upperCommand.indexOf("ASK");

        if (askIndex == -1) {
            return "ERROR: Invalid ASK command";
        }

        int promptStart = askIndex + 3; // Length of "ASK"
        if (promptStart >= command.length()) {
            return "ERROR: ASK requires a prompt";
        }

        String prompt = command.substring(promptStart).trim();
        if (prompt.isEmpty()) {
            return "ERROR: ASK requires a prompt";
        }

        // Set prompt in memory for Assistant loop to pick up
        Assistant current = memory.getAssistant();
        memory.setAssistant(new Assistant(current.active(), current.status(), prompt));

        return "ASK prompt received: \"" + prompt + "\"";
    }

    private double normalizeHeading(double heading) {
        heading = heading % 360.0;
        if (heading < 0) {
            heading += 360.0;
        }
        return heading;
    }

    public boolean isLlmEnabled() {
        return llmEnabled;
    }

    public Memory getMemory() {
        return memory;
    }
}
