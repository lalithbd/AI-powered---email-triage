package org.galactic.adapter;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import org.galactic.dto.ollama.TriageResult;

@RegisterAiService
public interface OllamaAdapter {

    @SystemMessage(fromResource = "prompts/ClassifyRules_V1.txt")
    @UserMessage(fromResource = "prompts/ClassifyRequest.txt")
    TriageResult classify(String sender, String subject, String receivedAt, String body, int attachmentCount);
}
