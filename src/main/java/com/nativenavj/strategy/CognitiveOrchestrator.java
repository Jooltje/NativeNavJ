package com.nativenavj.strategy;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import com.nativenavj.ai.FlightTools;
import com.nativenavj.domain.Shell;
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

    public CognitiveOrchestrator(Shell shell, SafetyGuardrails safety) {
        String modelName = "llama3";
        String baseUrl = "http://localhost:11434";

        this.tools = new FlightTools(shell, safety);
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
        // Check if LLM should handle this (only if LLM is enabled and online)
        if (online && agent != null && tools.getShell().isLlmEnabled()) {
            try {
                String response = agent.chat(command);
                LogManager.logControl("AI Response: " + response);
                return response;
            } catch (Exception e) {
                LogManager.error("AI Error, falling back to Shell", e);
            }
        }

        // Use Shell for all command parsing
        return tools.getShell().execute(command);
    }

    /**
     * Gets the Shell instance for direct access.
     */
    public Shell getShell() {
        return tools.getShell();
    }
}
