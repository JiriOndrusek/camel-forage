package io.kaoto.forage.model.quarkus.runtime;

import io.kaoto.forage.core.ai.ModelProvider;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

/**
 * Quarkus chat model has to be wrapped into io.kaoto.forage.core.ai.ModelProvider
 */
@Recorder
public class ForageModelQuarkusRecorder {
    private static final Logger LOG = Logger.getLogger(ForageModelQuarkusRecorder.class);

    public RuntimeValue<ModelProvider> createModelProvider(String name) {

        return switch (name) {
            case "ollama" -> new RuntimeValue<>(new QuarkusModelOllamaProvider());
            default -> throw new RuntimeException("Unknown model provider: %s".formatted(name));
        };
    }
}
