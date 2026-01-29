package io.kaoto.forage.model.quarkus.runtime;

import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.core.ai.ModelProvider;
import io.kaoto.forage.core.annotations.ForageBean;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;

// todo create providers for all types, difference is only the annotation value, which is required to match the type of
// the chat model
@ForageBean(
        value = "ollama",
        components = {"camel-langchain4j-agent"},
        feature = "Chat Model",
        description = "Locally-hosted models via Ollama (Llama, Mistral, etc.)")
@ApplicationScoped
public class QuarkusModelOllamaProvider implements ModelProvider {

    @Override
    public ChatModel create(String s) {
        // todo make generic
        InstanceHandle<ChatModel> handle = Arc.container().instance(ChatModel.class);
        return handle.get();
    }
}
