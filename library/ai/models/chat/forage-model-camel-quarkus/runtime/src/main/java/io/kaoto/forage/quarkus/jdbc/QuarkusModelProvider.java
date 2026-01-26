package io.kaoto.forage.quarkus.jdbc;

import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.core.ai.ModelProvider;
import io.kaoto.forage.core.annotations.ForageBean;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ForageBean(
        value = "ollama",
        components = {"camel-langchain4j-agent"},
        feature = "Chat Model",
        description = "Locally-hosted models via Ollama (Llama, Mistral, etc.)")
@ApplicationScoped
public class QuarkusModelProvider implements ModelProvider {

    @Inject
    List<ChatModel> models;

    private final String origId;

    public QuarkusModelProvider() {

        this.origId = "ollama";
    }

    public QuarkusModelProvider(String origId) {
        this.origId = origId;
    }

    @Override
    public ChatModel create(String s) {
        //        ConfigProvider.getConfig().getPropertyNames().forEach(System.out::println);
        //        return (ChatModel) ConfigProvider.getConfig().getConfigValue(s != null ? s : origId);

        InstanceHandle<ChatModel> handle = Arc.container().instance(ChatModel.class);
        return (ChatModel) handle.get();
    }
}
