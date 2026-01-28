package com.nativenavj.ai;

import com.nativenavj.domain.Memory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assistant Knowledge Source.
 * Polls for prompts in Memory and generates responses using LLM.
 */
public class Assistant implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Assistant.class);

    private final Memory memory;
    private final OllamaChatModel model;

    public Assistant(Memory memory) {
        this.memory = memory;

        String modelName = "llama3";
        String baseUrl = "http://localhost:11434";

        this.model = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

    public static Assistant inactive() {
        return new Assistant(null);
    }

    @Override
    public void run() {
        if (memory == null)
            return;

        com.nativenavj.domain.Assistant assistantState = memory.getAssistant();
        if (assistantState.active() && assistantState.status() == com.nativenavj.domain.Assistant.Status.THINKING) {
            processPrompt(assistantState.prompt());
        }
    }

    private void processPrompt(String prompt) {
        log.info("Processing assistant prompt: {}", prompt);
        try {
            String response = model.generate(prompt);
            log.info("Assistant Response: {}", response);
            memory.setAssistant(
                    new com.nativenavj.domain.Assistant(true, com.nativenavj.domain.Assistant.Status.IDLE, prompt));
        } catch (Exception e) {
            log.error("Failed to generate assistant response", e);
            memory.setAssistant(
                    new com.nativenavj.domain.Assistant(true, com.nativenavj.domain.Assistant.Status.IDLE, prompt));
        }
    }
}
