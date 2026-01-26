package io.kaoto.forage.quarkus.jdbc;

import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.core.ai.ModelProvider;
import org.eclipse.microprofile.config.ConfigProvider;

public class QuarkusModelProvider implements ModelProvider {

    private final String origId;

    public QuarkusModelProvider(String origId) {
        this.origId = origId;
    }

    @Override
    public ChatModel create(String s) {
        return (ChatModel)ConfigProvider.getConfig().getConfigValue(s != null ? s : origId);
    }
}
