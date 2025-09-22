package org.apache.camel.forage.quarkus.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.camel.forage.jdbc.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.MultiDataSourceConfig;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ForageDataSourceQuarkusConfigSource implements ConfigSource {

    private static final Map<String, String> configuration = new HashMap<>();

    static {
        // there is no need to check. whether property already exists, because the priority solves it

        // try loading multiDatasource properties
        MultiDataSourceConfig multiDataSourceConfig = new MultiDataSourceConfig();
        if (multiDataSourceConfig.multiDataSourceNames() != null
                && !multiDataSourceConfig.multiDataSourceNames().isEmpty()) {
            for (String name : multiDataSourceConfig.multiDataSourceNames()) {
                DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                configureDs(name, dsFactoryConfig);
            }
        } else {
            DataSourceFactoryConfig dataSourceFactoryConfig = new DataSourceFactoryConfig();
            configureDs("datasource", dataSourceFactoryConfig);
        }
    }

    private static void configureDs(String prefix, DataSourceFactoryConfig config) {
        //if provider datasource class differs form postgresql, ignore it (this means thar the multi ds for another
        // db-type is present)


        configuration.put("quarkus.datasource.db-kind", "postgresql");
        configuration.put("quarkus.datasource.password", config.password());
        configuration.put("quarkus.datasource.username", config.username());
        configuration.put("quarkus.datasource.jdbc.url", config.jdbcUrl());
        configuration.put("quarkus.datasource.jdbc.max-size", config.maxSize() + "");
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
