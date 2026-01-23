package com.nativenavj.strategy;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import com.nativenavj.ai.FlightTools;
import com.nativenavj.control.FlightController;
import com.nativenavj.safety.SafetyGuardrails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CognitiveOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(CognitiveOrchestrator.class);

    public interface PilotAssistant {
        String chat(String userMessage);
    }

    private final PilotAssistant agent;

    public CognitiveOrchestrator(FlightController controller, SafetyGuardrails safety) {
        String modelName = "llama3"; // Default model
        String baseUrl = "http://localhost:11434";

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.2)
                .build();

        FlightTools tools = new FlightTools(controller, safety);

        this.agent = AiServices.builder(PilotAssistant.class)
                .chatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .tools(tools)
                .build();

        logger.info("CognitiveOrchestrator initialized with model: {}", modelName);
    }

    public String issueCommand(String command) {
        logger.info("User Command: {}", command);
        String response = agent.chat(command);
        logger.info("AI Response: {}", response);
        return response;
    }
}
