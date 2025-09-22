package org.apache.camel.forage.quarkus.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.DataSourceFactoryConfig;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ForageDataSourceQuarkusConfigSource implements ConfigSource {

    private static final Map<String, String> configuration = new HashMap<>();

    static {
        // there is no need to check. whether property already exists, because the priority solves it

        // try loading multiDatasource properties
        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Set<String> prefixes = ConfigStore.getInstance().readPrefixes(config, "(.+).jdbc\\..*");

        if (!prefixes.isEmpty()) {
            for (String name : prefixes) {
                DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                configureDs(name, dsFactoryConfig);
            }
        } else {
            configureDs("datasource", config);
        }
    }

    private static void configureDs(String prefix, DataSourceFactoryConfig config) {
        // if provider datasource class differs form postgresql, ignore it (this means thar the multi ds for another
        // db-type is present)

        // todo
        configuration.put(String.format("quarkus.%s.db-kind", prefix), "postgresql");
        configuration.put(String.format("quarkus.%s.password", prefix), config.password());
        configuration.put(String.format("quarkus.%s.username", prefix), config.username());
        configuration.put(String.format("quarkus.%s.jdbc.url", prefix), config.jdbcUrl());
        configuration.put(String.format("quarkus.%s.jdbc.max-size", prefix), config.maxSize() + "");
    }

    /**
     *
     * System Properties    400
     * Environment Variables from System 300
     * Environment Variables from .env file 295
     * InMemoryConfigSource 275
     * application.properties from /config 260
     * application.properties from application 250
     * microprofile-config.properties from application 100
     */
    @Override
    public int getOrdinal() {
        return 410;
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getValue(final String propertyName) {
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return ForageDataSourceQuarkusConfigSource.class.getSimpleName();
    }
}
