package io.kaoto.forage.vectordb.inmemory;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.kaoto.forage.core.ai.EmbeddingStoreWithModelProvider;
import io.kaoto.forage.core.annotations.ForageBean;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ForageBean(
        value = "inMemoryStore",
        components = {"camel-langchain4j-embeddings"},
        description = "MariaDB with vector support")
public class InMemoryStoreProvider implements EmbeddingStoreWithModelProvider {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryStoreProvider.class);

    private EmbeddingModel embeddingModel;

    @Override
    public InMemoryStoreProvider withEmbeddingModel(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        return this;
    }

    @Override
    public EmbeddingStore<TextSegment> create(String id) {
        final InMemoryStoreConfig config = new InMemoryStoreConfig(id);

        if (embeddingModel == null) {
            LOG.trace("embeddingModel is mandatory for InMemoryStore creation");
            return null;
        }

        String fileSource = config.fileSource();
        Integer maxSize = config.maxSize();
        Integer overlapSize = config.overlapSize();

        LOG.trace(
                "Creating InMemory embedding store from {} with configuration: maxSize={}, overlapSize={}",
                fileSource,
                maxSize,
                overlapSize);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(fileSource)) {
            if (stream == null) {
                LOG.trace("InMemory embedding store is not created. The source file is not provided.");
                new IllegalArgumentException("company-knowledge.txt not found");
            }

            Document document = Document.from(new String(stream.readAllBytes(), StandardCharsets.UTF_8));

            List<TextSegment> segments =
                    DocumentSplitters.recursive(maxSize, overlapSize).split(document);

            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            // Store in embedding store
            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
            embeddingStore.addAll(embeddings, segments);

            return embeddingStore;
        } catch (IOException e) {
            throw new RuntimeException("Non accessible source file '%s'".formatted(fileSource), e);
        }
    }
}
