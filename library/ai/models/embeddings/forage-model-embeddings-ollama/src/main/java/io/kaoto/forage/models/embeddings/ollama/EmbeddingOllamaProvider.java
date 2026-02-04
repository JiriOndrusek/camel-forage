package io.kaoto.forage.models.embeddings.ollama;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import io.kaoto.forage.core.annotations.ForageBean;
import java.time.Duration;

import io.kaoto.forage.core.common.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * todo
 */
@ForageBean(
        value = "ollama",
        components = {"camel-langchain4j-agent"},
        feature = "Embeddings Model",
        description = "Locally-hosted models via Ollama (Llama, Mistral, etc.)")
public class EmbeddingOllamaProvider implements BeanProvider<EmbeddingModel> {
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddingOllamaProvider.class);

    /**
     * Creates a new Ollama chat model instance with the configured parameters.
     *
     * <p>This method creates an {@link OllamaChatModel} using the base URL and
     * model name from the configuration. The model is ready to use for chat
     * operations once created.
     *
     * @return a new configured Ollama chat model instance
     */
    @Override
    public EmbeddingModel create(String id) {
        final EmbeddingOllamaConfig config = new EmbeddingOllamaConfig(id);

        String baseUrl = config.baseUrl();
        String modelName = config.modelName();
        // todo        Double temperature = config.customHeaders();
        Integer maxRetries = config.maxRetries();
        Duration timeout = config.timeout();
        Boolean logRequests = config.logRequests();
        Boolean logResponses = config.logResponses();

        LOG.trace(
                "Creating Ollama model: {} at {} with configuration: maxRetries={}, toptimeoutK={}, logRequests={}, logResponses={}",
                modelName,
                baseUrl,
                maxRetries,
                timeout,
                logRequests,
                logResponses);

        OllamaEmbeddingModel.OllamaEmbeddingModelBuilder builder =
                OllamaEmbeddingModel.builder().baseUrl(baseUrl).modelName(modelName);
        // Only set optional parameters if they are configured
        if (maxRetries != null) {
            builder.maxRetries(maxRetries);
        }

        if (timeout != null) {
            builder.timeout(timeout);
        }
        if (logRequests != null) {
            builder.logRequests(logRequests);
        }

        if (logResponses != null) {
            builder.logResponses(logResponses);
        }

        return builder.build();
    }
}
