package io.kaoto.forage.core.ai;

import dev.langchain4j.model.embedding.EmbeddingModel;

public interface EmbeddingModelAware {

    void withEmbeddingModel(EmbeddingModel embeddingModel);
}
