package io.kaoto.forage.quarkus.jdbc;

import io.kaoto.forage.core.ai.ModelProvider;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import org.jboss.logging.Logger;

/**
 * Aggregation repository is created via Recorder
 */
@Recorder
public class ForageQuarkusChatModelsRecorder {
    private static final org.jboss.logging.Logger LOG = Logger.getLogger(ForageQuarkusChatModelsRecorder.class);

    public RuntimeValue<ModelProvider> createModelProvider(String name) {

        return new RuntimeValue<>(new QuarkusModelProvider(name));
    }
}
