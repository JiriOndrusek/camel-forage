package io.kaoto.forage.vectordb.inmemory;

import static io.kaoto.forage.vectordb.inmemory.InMemoryStoreConfigEntries.*;

import io.kaoto.forage.core.util.config.Config;
import io.kaoto.forage.core.util.config.ConfigModule;
import io.kaoto.forage.core.util.config.ConfigStore;
import java.util.Optional;

/**
 * todo
 */
public class InMemoryStoreConfig implements Config {

    //    private static final int DEFAULT_DIMENSION = 384;

    private final String prefix;

    /**
     * Creates a new InMemory configuration using default (non-prefixed) properties.
     */
    public InMemoryStoreConfig() {
        this(null);
    }

    /**
     * todo
     **/
    public InMemoryStoreConfig(String prefix) {
        this.prefix = prefix;

        // First register new configuration modules. This happens only if a prefix is provided
        InMemoryStoreConfigEntries.register(prefix);

        // Then, loads the configurations from the properties file associated with this Config module
        ConfigStore.getInstance().load(InMemoryStoreConfig.class, this, this::register);

        // Lastly, load the overrides defined in system properties and environment variables
        InMemoryStoreConfigEntries.loadOverrides(prefix);
    }

    @Override
    public String name() {
        return "forage-vectordb-in-memmory";
    }

    /**
     * todo
     */
    public String fileSource() {
        return ConfigStore.getInstance().get(FILE_SOURCE.asNamed(prefix)).orElse(null);
    }

    /**
     * todo
     */
    public Integer maxSize() {
        return ConfigStore.getInstance()
                .get(MAX_SIZE.asNamed(prefix))
                .map(Integer::parseInt)
                .orElse(null);
    }
    /**
     * todo
     */
    public Integer overlapSize() {
        return ConfigStore.getInstance()
                .get(OVERLAP_SIZE.asNamed(prefix))
                .map(Integer::parseInt)
                .orElse(null);
    }

    @Override
    public void register(String name, String value) {
        Optional<ConfigModule> config = InMemoryStoreConfigEntries.find(prefix, name);

        config.ifPresent(module -> ConfigStore.getInstance().set(module, value));
    }
}
