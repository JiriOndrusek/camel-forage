package io.kaoto.forage.agent.simple;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ForageAgentWithMemory {

    /**
     * Simple AI service interface without memory support
     */
    String chat(@MemoryId Object memoryId, @UserMessage String message);

    @SystemMessage("{{prompt}}")
    String chat(@MemoryId Object memoryId, @UserMessage String message, @V("prompt") String prompt);
}
