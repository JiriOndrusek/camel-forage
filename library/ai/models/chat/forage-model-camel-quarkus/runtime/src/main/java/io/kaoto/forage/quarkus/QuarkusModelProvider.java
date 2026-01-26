package io.kaoto.forage.quarkus;

import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.core.ai.ModelProvider;
import io.kaoto.forage.core.annotations.ForageBean;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;

// todo create providers for all bean, difference is only the annotation value, which is required to match the type of
// the chat model
@ForageBean(
        value = "ollama",
        components = {"camel-langchain4j-agent"},
        feature = "Chat Model",
        description = "Locally-hosted models via Ollama (Llama, Mistral, etc.)")
@ApplicationScoped
public class QuarkusModelProvider implements ModelProvider {

    private final String origId;

    public QuarkusModelProvider() {
        this.origId = null;
    }

    public QuarkusModelProvider(String origId) {
        this.origId = origId;
    }

    @Override
    public ChatModel create(String s) {

        // todo make generic
        InstanceHandle<ChatModel> handle = Arc.container().instance(ChatModel.class);
        return handle.get();
    }
}
