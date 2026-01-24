package com.nativenavj.strategy;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import com.nativenavj.ai.FlightTools;
// import com.nativenavj.control.FlightController; // REMOVED - use Coordinator
import com.nativenavj.safety.SafetyGuardrails;
import com.nativenavj.util.LogManager;

import java.net.HttpURLConnection;
import java.net.URL;

public class CognitiveOrchestrator {

    public interface PilotAssistant {
        String chat(String userMessage);
    }

    private final PilotAssistant agent;
    private final FlightTools tools;
    private boolean online = false;

    public CognitiveOrchestrator(Object controller, SafetyGuardrails safety) {
        String modelName = "llama3";
        String baseUrl = "http://localhost:11434";

        this.tools = new FlightTools(controller, safety);
        this.online = checkConnection(baseUrl);

        if (online) {
            try {
                OllamaChatModel model = OllamaChatModel.builder()
                        .baseUrl(baseUrl)
                        .modelName(modelName)
                        .temperature(0.2)
                        .build();

                this.agent = AiServices.builder(PilotAssistant.class)
                        .chatLanguageModel(model)
                        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                        .tools(tools)
                        .build();
                LogManager.info("CognitiveOrchestrator: Connected to Ollama (" + modelName + ").");
            } catch (Exception e) {
                LogManager.error("Failed to initialize AI Services", e);
                this.online = false;
                throw e;
            }
        } else {
            this.agent = null;
            LogManager.warn("CognitiveOrchestrator: Ollama not found. Falling back to rule-based parser.");
        }
    }

    private boolean checkConnection(String baseUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public String issueCommand(String command) {
        if (online && agent != null) {
            try {
                String response = agent.chat(command);
                LogManager.logControl("AI Response: " + response);
                return response;
            } catch (Exception e) {
                LogManager.error("AI Error, falling back to rule-based", e);
            }
        }
        return processFallback(command);
    }

    private String processFallback(String command) {
        String input = command.toUpperCase();
        try {
            if (input.contains("STOP") || input.contains("OFF") || input.contains("MANUAL")) {
                tools.disableAll();
                return "EMERGENCY STOP: All flight controls disabled. Returning to manual control.";
            } else if (input.contains("MAINTAIN") || input.contains("KEEP") || input.equals("ON")) {
                tools.maintainCurrentFlight();
                return "Maintaining current flight state (Syncing targets...).";
            } else if (input.contains("HEAD") || input.contains("HEADING")) {
                int heading = Integer.parseInt(input.replaceAll("[^0-9]", ""));
                tools.setHeading(heading);
                return "Set heading to " + heading;
            } else if (input.contains("ALT") || input.contains("ALTITUDE")) {
                int alt = Integer.parseInt(input.replaceAll("[^0-9]", ""));
                tools.setAltitude(alt);
                return "Set altitude to " + alt;
            } else if (input.contains("SPEED") || input.contains("AIRSPEED")) {
                int speed = Integer.parseInt(input.replaceAll("[^0-9]", ""));
                tools.setAirspeed(speed);
                return "Set airspeed to " + speed;
            }
        } catch (Exception e) {
            LogManager.error("Fallback parser failed for: " + command);
        }
        return "Unknown command: '" + command + "'. Try 'Set Altitude 5000' or 'STOP'.";
    }
}
