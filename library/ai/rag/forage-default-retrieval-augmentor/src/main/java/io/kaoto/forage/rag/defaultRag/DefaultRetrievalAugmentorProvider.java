package io.kaoto.forage.rag.defaultRag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.kaoto.forage.core.annotations.ForageBean;
import io.kaoto.forage.core.ai.RetrievalAugmentorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * todo
 */
@ForageBean(
        value = "defaultRag",
        components = {"camel-langchain4j-agent"},
        feature = "RAG",
        description = "Default retrieval augmentor for naive rag scenario")
public class DefaultRetrievalAugmentorProvider implements RetrievalAugmentorProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRetrievalAugmentorProvider.class);

    private EmbeddingStore<?> embeddingStore;
    private EmbeddingModel embeddingModel;

    @Override
    public RetrievalAugmentorProvider withEmbeddingStore(EmbeddingStore<?> embeddingStore) {
        this.embeddingStore = embeddingStore;
        return this;
    }

    @Override
    public RetrievalAugmentorProvider withEmbeddingModel(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        return this;
    }

    /**
     * todo
     */
    @Override
    public RetrievalAugmentor create(String id) {
                final DefaultRetrievalAugmentorConfig config = new DefaultRetrievalAugmentorConfig(id);

        int maxResults = config.maxResults();
        double minScore = config.minScore();


        // Create content retriever
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                //todo proper handling
                .embeddingStore((EmbeddingStore<TextSegment>) embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        LOG.trace(
                "Creating DefaultRetrievalAugmentor model with configuration: maxResults={}, minScore={}",
                maxResults,
                minScore);

        // Create a RetrievalAugmentor that uses only a content retriever : naive rag scenario
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();




//
//        OllamaEmbeddingModel.OllamaEmbeddingModelBuilder builder =
//                OllamaEmbeddingModel.builder().baseUrl(baseUrl).modelName(modelName);
//        // Only set optional parameters if they are configured
//        if (maxRetries != null) {
//            builder.maxRetries(maxRetries);
//        }
//
//        if (timeout != null) {
//            builder.timeout(timeout);
//        }
//        if (logRequests != null) {
//            builder.logRequests(logRequests);
//        }
//
//        if (logResponses != null) {
//            builder.logResponses(logResponses);
//        }
//
//        return builder.build();

    }

}
