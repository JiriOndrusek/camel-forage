package org.apache.camel.forage.quarkus.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jms.common.ConnectionFactoryConfig;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForageJmsConfigSource implements ConfigSource {
    private static final Logger LOG = LoggerFactory.getLogger(ForageJmsConfigSource.class);
    private static final Map<String, String> configuration = new HashMap<>();

    static {
        // there is no need to check. whether property already exists, because the priority solves it

        // try loading multiDatasource properties
        ConnectionFactoryConfig config = new ConnectionFactoryConfig();
        Set<String> prefixes = ConfigStore.getInstance().readPrefixes(config, "(.+).jms\\..*");

        if (!prefixes.isEmpty()) {
            for (String name : prefixes) {
                ConnectionFactoryConfig connectionFactoryConfig = new ConnectionFactoryConfig(name);
                configureDs(name, connectionFactoryConfig);
            }
        } else if (!ConfigStore.getInstance().readPrefixes(config, "jms\\..*").isEmpty()) {
            ;
            configureDs("dataSource", config);
        } else {
            LOG.trace("No jms config found.");
        }
    }

    private static void configureDs(String prefix, ConnectionFactoryConfig config) {
        String property = "quarkus.artemis.";
        if (prefix != null && !prefix.isEmpty()) {
            property = property + "\"" + prefix + "\".";
        }

        configuration.put("quarkus.artemis.enabled", "true");
        configuration.put(property + "password", config.password());
        configuration.put(property + "username", config.username());
        configuration.put(property + "url", config.brokerUrl());
        //        configuration.put(property + "jdbc.initial-size", String.valueOf(config.initialSize()));
        //        configuration.put(property + "jdbc.min-size", String.valueOf(config.minSize()));
        //        configuration.put(property + "jdbc.max-size", String.valueOf(config.minSize()));
        //        configuration.put(property + "jdbc.acquisition-timeout", config.acquisitionTimeoutSeconds() + "S");
        //        configuration.put(property + "jdbc.validation-query-sql", config.validationTimeoutSeconds() + "S");
        //        configuration.put(property + "jdbc.leak-detection-interval", config.leakTimeoutMinutes() + "M");
        //
        //        if (config.transactionEnabled()) {
        //            configuration.put("quarkus.datasource.transaction-isolation", "READ_COMMITTED");
        //            configuration.put(
        //                    "quarkus.transaction-manager.default-transaction-timeout",
        //                    config.transactionTimeoutSeconds() + "S");
        //            if (config.transactionNodeId() != null) {
        //                configuration.put("quarkus.transaction-manager.node-name", config.transactionNodeId());
        //            }
        //             Quarkus does not support transactionObjectStoreId
        //            configuration.put(
        //                    "quarkus.transaction-manager.enable-recovery",
        // String.valueOf(config.transactionEnableRecovery()));
        //            configuration.put("quarkus.transaction-manager.recovery-modules",
        // config.transactionRecoveryModules());
        //            configuration.put(
        //                    "quarkus.transaction-manager.xa-resource-orphan-filters",
        //                    config.transactionXaResourceOrphanFilters());
        //            configuration.put(
        //                    "quarkus.transaction-manager.object-store.directory",
        // config.transactionObjectStoreDirectory());
        //            configuration.put("quarkus.transaction-manager.object-store.type",
        // config.transactionObjectStoreType());
        //            if (config.transactionObjectStoreDataSource() != null) {
        //                configuration.put(
        //                        "quarkus.transaction-manager.object-store.datasource",
        //                        config.transactionObjectStoreDataSource());
        //            }
        //            configuration.put(
        //                    "quarkus.transaction-manager.object-store.drop-table",
        //                    String.valueOf(config.transactionObjectStoreDropTable()));
        //            configuration.put(
        //                    "quarkus.transaction-manager.object-store.table-prefix",
        //                    config.transactionObjectStoreTablePrefix());
        //        }
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
        return ForageJmsConfigSource.class.getSimpleName();
    }
}
