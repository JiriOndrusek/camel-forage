package io.kaoto.forage.rag.defaultRag;

import static io.kaoto.forage.rag.defaultRag.DefaultRetrievalAugmentorConfigEntries.*;

import io.kaoto.forage.core.util.config.Config;
import io.kaoto.forage.core.util.config.ConfigModule;
import io.kaoto.forage.core.util.config.ConfigStore;
import java.util.Optional;

/**
 * todo
 */
public class DefaultRetrievalAugmentorConfig implements Config {

    private final String prefix;

    /**
     * todo
     */
    public DefaultRetrievalAugmentorConfig() {
        this(null);
    }

    public DefaultRetrievalAugmentorConfig(String prefix) {
        this.prefix = prefix;

        // First register new configuration modules. This happens only if a prefix is provided
        DefaultRetrievalAugmentorConfigEntries.register(prefix);

        // Then, loads the configurations from the properties file associated with this Config module
        ConfigStore.getInstance().load(DefaultRetrievalAugmentorConfig.class, this, this::register);

        // Lastly, load the overrides defined in system properties and environment variables
        DefaultRetrievalAugmentorConfigEntries.loadOverrides(prefix);
    }

    @Override
    public void register(String name, String value) {
        Optional<ConfigModule> config = DefaultRetrievalAugmentorConfigEntries.find(prefix, name);

        config.ifPresent(module -> ConfigStore.getInstance().set(module, value));
    }

    /**
     * todo
     */
    @Override
    public String name() {
        return "forage-rag-default";
    }

    /**
     * todo
     */
    public Integer maxResults() {
        return ConfigStore.getInstance()
                .get(MAX_RESULTS.asNamed(prefix))
                .map(Integer::parseInt)
                .orElse(null);
    }

    /**
     * todo
     */
    public Double minScore() {
        return ConfigStore.getInstance()
                .get(MIN_SCORE.asNamed(prefix))
                .map(Double::parseDouble)
                .orElse(null);
    }
}
