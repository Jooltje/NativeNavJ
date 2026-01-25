package com.nativenavj.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import com.nativenavj.control.Loop;
import com.nativenavj.domain.Memory;
import com.nativenavj.domain.Shell;
import com.nativenavj.port.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

/**
 * Assistant Knowledge Source.
 * Provides natural language interaction using local LLM.
 */
public class Assistant extends Loop {
    private static final Logger log = LoggerFactory.getLogger(Assistant.class);

    public interface PilotAssistant {
        String chat(String userMessage);
    }

    private final PilotAssistant agent;
    private final FlightTools tools;
    private final Shell shell;
    private final Memory memory;
    private boolean online = false;

    public Assistant(Shell shell, Memory memory, Clock clock) {
        super(1.0, clock); // Check status/process at 1Hz
        this.shell = shell;
        this.memory = memory;
        this.tools = new FlightTools(shell);

        String modelName = "llama3";
        String baseUrl = "http://localhost:11434";

        this.online = checkConnection(baseUrl);

        PilotAssistant localAgent = null;
        if (online) {
            try {
                OllamaChatModel model = OllamaChatModel.builder()
                        .baseUrl(baseUrl)
                        .modelName(modelName)
                        .temperature(0.2)
                        .build();

                localAgent = AiServices.builder(PilotAssistant.class)
                        .chatLanguageModel(model)
                        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                        .tools(tools)
                        .build();
                log.info("Assistant: Connected to Ollama ({}).", modelName);
            } catch (Exception e) {
                log.error("Failed to initialize AI Services", e);
                this.online = false;
            }
        } else {
            log.warn("Assistant: Ollama not found. Falling back to rule-based parser.");
        }
        this.agent = localAgent;

        updateStatus();
    }

    private boolean checkConnection(String baseUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) java.net.URI.create(baseUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateStatus() {
        com.nativenavj.domain.Assistant current = memory.getAssistant();
        com.nativenavj.domain.Assistant status = new com.nativenavj.domain.Assistant(
                current.active(),
                online ? "READY" : "OFFLINE",
                current.prompt());
        memory.setAssistant(status);
    }

    public String issueCommand(String command) {
        if (online && agent != null && shell.isLlmEnabled()) {
            try {
                String response = agent.chat(command);
                log.info("AI Response: {}", response);
                return response;
            } catch (Exception e) {
                log.error("AI Error, falling back to Shell", e);
            }
        }
        return shell.execute(command);
    }

    @Override
    protected void step() {
        // Periodic status check
        boolean currentOnline = checkConnection("http://localhost:11434");
        if (currentOnline != online) {
            this.online = currentOnline;
            updateStatus();
        }

        // Process prompt if present in memory and LLM enabled
        com.nativenavj.domain.Assistant current = memory.getAssistant();
        if (online && agent != null && current.active() && !current.prompt().isEmpty()) {
            try {
                log.info("Processing async prompt: {}", current.prompt());
                String response = agent.chat(current.prompt());
                log.info("AI Async Response: {}", response);
                System.out.println("CO-PILOT > " + response);

                // Clear prompt after processing
                memory.setAssistant(new com.nativenavj.domain.Assistant(current.active(), current.status(), ""));
            } catch (Exception e) {
                log.error("AI Async Error", e);
            }
        }
    }
}
