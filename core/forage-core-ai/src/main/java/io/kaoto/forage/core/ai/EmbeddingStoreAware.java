package io.kaoto.forage.core.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

public interface EmbeddingStoreAware {

    void withEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore);
}
