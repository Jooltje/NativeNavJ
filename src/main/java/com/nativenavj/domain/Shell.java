package com.nativenavj.domain;

import com.nativenavj.control.Computer;

/**
 * Shell for parsing and executing user commands.
 * Implements the User -> Shell -> Computer flow.
 */
public class Shell {
    private final Computer computer;
    private boolean llmEnabled;

    public Shell(Computer computer) {
        this.computer = computer;
        this.llmEnabled = false;
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
                computer.activate();
                yield "System enabled";
            }
            case "OFF" -> {
                computer.deactivate();
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
            // Normalize heading to 0-360 range
            heading = normalizeHeading(heading);
            computer.setHeading(heading);
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
            computer.setAltitude(altitude);
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
            computer.setSpeed(speed);
            return String.format("Airspeed set to %.0f knots", speed);
        } catch (NumberFormatException e) {
            return "ERROR: Invalid airspeed value '" + parts[1] + "'";
        }
    }

    private String executeLlm(String[] parts) {
        if (parts.length < 2) {
            return "ERROR: LLM requires ON or OFF argument";
        }

        return switch (parts[1]) {
            case "ON" -> {
                llmEnabled = true;
                yield "LLM control enabled";
            }
            case "OFF" -> {
                llmEnabled = false;
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

        // TODO: Forward to CognitiveOrchestrator when integrated
        return "ASK prompt received: \"" + prompt + "\" (LLM integration pending)";
    }

    /**
     * Normalizes heading to 0-360 degree range.
     */
    private double normalizeHeading(double heading) {
        heading = heading % 360.0;
        if (heading < 0) {
            heading += 360.0;
        }
        return heading;
    }

    /**
     * Checks if LLM mode is enabled.
     */
    public boolean isLlmEnabled() {
        return llmEnabled;
    }

    /**
     * Gets the computer for direct access.
     */
    public Computer getComputer() {
        return computer;
    }
}
