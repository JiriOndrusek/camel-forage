package io.kaoto.forage.models.embeddings.ollama;

import static io.kaoto.forage.models.embeddings.ollama.EmbeddingOllamaConfigEntries.*;

import io.kaoto.forage.core.util.config.Config;
import io.kaoto.forage.core.util.config.ConfigModule;
import io.kaoto.forage.core.util.config.ConfigStore;
import java.time.Duration;
import java.util.Optional;

/**
 * todo
 */
public class EmbeddingOllamaConfig implements Config {

    private final String prefix;

    /**
     * Constructs a new OllamaConfig and registers configuration parameters with the ConfigStore.
     *
     * <p>During construction, this class:
     * <ul>
     *   <li>Registers the base URL configuration to be sourced from OLLAMA_BASE_URL environment variable</li>
     *   <li>Registers the model name configuration to be sourced from OLLAMA_MODEL_NAME environment variable</li>
     *   <li>Registers advanced parameters (temperature, topK, topP, minP, numCtx) from their respective environment variables</li>
     *   <li>Registers logging parameters (logRequests, logResponses) from their respective environment variables</li>
     *   <li>Attempts to load additional properties from forage-model-ollama.properties</li>
     * </ul>
     *
     * <p>Configuration values are resolved when this constructor is called, with default values
     * used when no configuration is provided through environment variables, system properties,
     * or configuration files.
     */
    public EmbeddingOllamaConfig() {
        this(null);
    }

    public EmbeddingOllamaConfig(String prefix) {
        this.prefix = prefix;

        // First register new configuration modules. This happens only if a prefix is provided
        EmbeddingOllamaConfigEntries.register(prefix);

        // Then, loads the configurations from the properties file associated with this Config module
        ConfigStore.getInstance().load(EmbeddingOllamaConfig.class, this, this::register);

        // Lastly, load the overrides defined in system properties and environment variables
        EmbeddingOllamaConfigEntries.loadOverrides(prefix);
    }

    @Override
    public void register(String name, String value) {
        Optional<ConfigModule> config = EmbeddingOllamaConfigEntries.find(prefix, name);

        config.ifPresent(module -> ConfigStore.getInstance().set(module, value));
    }

    /**
     * Returns the unique identifier for this Ollama configuration module.
     *
     * <p>This name corresponds to the module artifact and is used for:
     * <ul>
     *   <li>Loading configuration files (forage-model-ollama.properties)</li>
     *   <li>Identifying this module in logs and error messages</li>
     *   <li>Distinguishing this configuration from other AI model configurations</li>
     * </ul>
     *
     * @return the module name "forage-model-ollama"
     */
    @Override
    public String name() {
        return "forage-embedding-model-ollama";
    }

    /**
     * Returns the base URL of the Ollama server.
     *
     * <p>This method retrieves the base URL that was configured through environment variables,
     * system properties, or configuration files. The base URL specifies where the Ollama
     * server is running and should include the protocol and port.
     *
     * <p><strong>Configuration Sources (in order of precedence):</strong>
     * <ol>
     *   <li>OLLAMA_BASE_URL environment variable</li>
     *   <li>ollama.base.url system property</li>
     *   <li>base-url property in forage-model-ollama.properties</li>
     *   <li>Default value: "http://localhost:11434"</li>
     * </ol>
     *
     * <p><strong>Example Values:</strong>
     * <ul>
     *   <li>"http://localhost:11434" - Local Ollama installation</li>
     *   <li>"http://ollama-server:11434" - Remote server</li>
     *   <li>"https://my-ollama.example.com" - HTTPS endpoint</li>
     * </ul>
     *
     * @return the Ollama server base URL, never null
     */
    public String baseUrl() {
        return ConfigStore.getInstance().get(BASE_URL.asNamed(prefix)).orElse(BASE_URL.defaultValue());
    }

    /**
     * Returns the name of the Ollama model to use.
     *
     * <p>This method retrieves the model name that specifies which Ollama model
     * should be used for AI operations. The model must be available on the
     * configured Ollama server.
     *
     * <p><strong>Common Model Names:</strong>
     * <ul>
     *   <li><strong>llama3</strong> - Meta's Llama 3 model (8B parameters)</li>
     *   <li><strong>llama3.1</strong> - Updated Llama 3.1 model</li>
     *   <li><strong>mistral</strong> - Mistral 7B model</li>
     *   <li><strong>codellama</strong> - Code-specialized Llama model</li>
     *   <li><strong>phi3</strong> - Microsoft's Phi-3 model</li>
     * </ul>
     *
     * <p><strong>Configuration Sources (in order of precedence):</strong>
     * <ol>
     *   <li>OLLAMA_MODEL_NAME environment variable</li>
     *   <li>ollama.model.name system property</li>
     *   <li>model-name property in forage-model-ollama.properties</li>
     *   <li>Default value: "llama3"</li>
     * </ol>
     *
     * @return the Ollama model name, never null
     */
    public String modelName() {
        return ConfigStore.getInstance().get(MODEL_NAME.asNamed(prefix)).orElse(MODEL_NAME.defaultValue());
    }

    //    /**
    //    todo
    //     */
    //    public Double temperature() {
    //        return ConfigStore.getInstance()
    //                .get(CUSTOM_HEADERS.asNamed(prefix))
    //                .map(Double::parseDouble)
    //                .orElse(null);
    //    }

    /**
     * todo
     */
    public Integer maxRetries() {
        return ConfigStore.getInstance()
                .get(MAX_RETRIES.asNamed(prefix))
                .map(Integer::parseInt)
                .orElse(null);
    }

    /**
     *
     */
    public Duration timeout() {
        return ConfigStore.getInstance()
                .get(TIMEOUT.asNamed(prefix))
                .map(Duration::parse)
                .orElse(null);
    }

    /**
     * Returns whether request logging is enabled.
     *
     * <p>When enabled, the Ollama client will log all requests sent to the server.
     * This is useful for debugging and monitoring but should be disabled in production
     * to avoid logging sensitive information.
     *
     * <p><strong>Configuration Sources (in order of precedence):</strong>
     * <ol>
     *   <li>OLLAMA_LOG_REQUESTS environment variable</li>
     *   <li>ollama.log.requests system property</li>
     *   <li>log-requests property in forage-model-ollama.properties</li>
     *   <li>No default value (returns null if not configured)</li>
     * </ol>
     *
     * <p><strong>Valid Values:</strong> "true" or "false" (case-insensitive)
     *
     * @return true if request logging is enabled, false if disabled, null if not configured
     */
    public Boolean logRequests() {
        return ConfigStore.getInstance()
                .get(LOG_REQUESTS.asNamed(prefix))
                .map(Boolean::parseBoolean)
                .orElse(null);
    }

    /**
     * Returns whether response logging is enabled.
     *
     * <p>When enabled, the Ollama client will log all responses received from the server.
     * This is useful for debugging and monitoring but should be disabled in production
     * to avoid logging sensitive information.
     *
     * <p><strong>Configuration Sources (in order of precedence):</strong>
     * <ol>
     *   <li>OLLAMA_LOG_RESPONSES environment variable</li>
     *   <li>ollama.log.responses system property</li>
     *   <li>log-responses property in forage-model-ollama.properties</li>
     *   <li>No default value (returns null if not configured)</li>
     * </ol>
     *
     * <p><strong>Valid Values:</strong> "true" or "false" (case-insensitive)
     *
     * @return true if response logging is enabled, false if disabled, null if not configured
     */
    public Boolean logResponses() {
        return ConfigStore.getInstance()
                .get(LOG_RESPONSES.asNamed(prefix))
                .map(Boolean::parseBoolean)
                .orElse(null);
    }
}
