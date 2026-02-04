package io.kaoto.forage.core.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.kaoto.forage.core.common.BeanProvider;

/**
 * Provider interface for creating AI embedding models
 */
public interface EmbeddingStoreWithModelProvider extends BeanProvider<EmbeddingStore<TextSegment>> {

    EmbeddingStoreWithModelProvider withEmbeddingModel(EmbeddingModel embeddingModel);
}
