package io.kaoto.forage.core.ai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.kaoto.forage.core.common.BeanProvider;

public interface RetrievalAugmentorProvider extends BeanProvider<RetrievalAugmentor> {

    RetrievalAugmentorProvider withEmbeddingStore(EmbeddingStore<?> embeddingStore);

    RetrievalAugmentorProvider withEmbeddingModel(EmbeddingModel embeddingModel);
}
