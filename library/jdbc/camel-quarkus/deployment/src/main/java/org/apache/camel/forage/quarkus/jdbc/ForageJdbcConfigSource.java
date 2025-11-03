package org.apache.camel.forage.quarkus.jdbc;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.forage.core.annotations.ForageFactory;
import org.apache.camel.forage.core.common.BeanFactory;
import org.apache.camel.forage.core.jdbc.DataSourceProvider;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.DataSourceBeanFactory;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.common.aggregation.ForageAggregationRepository;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

@ForageFactory(
        value = "CamelQuarkusDataSourceConfigSource",
        components = {"camel-sql", "camel-jdbc"},
        description = "Default Camel Quarkus DataSource config source",
        factoryType = "DataSource",
        autowired = true)
@ApplicationScoped
public class ForageJdbcConfigSource implements ConfigSource, BeanFactory {

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
            configureDs("dataSource", config);
        }
    }

    private static void configureDs(String prefix, DataSourceFactoryConfig config) {
        String property = "quarkus.datasource.";
        if (prefix != null && !prefix.isEmpty()) {
            property = property + "\"" + prefix + "\".";
        }

        configuration.put(property + "db-kind", config.dbKind());
        configuration.put(property + "password", config.password());
        configuration.put(property + "username", config.username());
        configuration.put(property + "jdbc.url", config.jdbcUrl());
        configuration.put(property + "jdbc.initial-size", String.valueOf(config.initialSize()));
        configuration.put(property + "jdbc.min-size", String.valueOf(config.minSize()));
        configuration.put(property + "jdbc.max-size", String.valueOf(config.minSize()));
        configuration.put(property + "jdbc.acquisition-timeout", config.acquisitionTimeoutSeconds() + "S");
        configuration.put(property + "jdbc.validation-query-sql", config.validationTimeoutSeconds() + "S");
        configuration.put(property + "jdbc.leak-detection-interval", config.leakTimeoutMinutes() + "M");

        if (config.transactionEnabled()) {
            configuration.put(
                    "quarkus.transaction-manager.default-transaction-timeout",
                    config.transactionTimeoutSeconds() + "S");
            if (config.transactionNodeId() != null) {
                configuration.put("quarkus.transaction-manager.node-name", config.transactionNodeId());
            }
            // Quarkus does not support transactionObjectStoreId
            configuration.put(
                    "quarkus.transaction-manager.enable-recovery", String.valueOf(config.transactionEnableRecovery()));
            configuration.put("quarkus.transaction-manager.recovery-modules", config.transactionRecoveryModules());
            configuration.put(
                    "quarkus.transaction-manager.xa-resource-orphan-filters",
                    config.transactionXaResourceOrphanFilters());
            configuration.put(
                    "quarkus.transaction-manager.object-store.directory", config.transactionObjectStoreDirectory());
            configuration.put("quarkus.transaction-manager.object-store.type", config.transactionObjectStoreType());
            if (config.transactionObjectStoreDataSource() != null) {
                configuration.put(
                        "quarkus.transaction-manager.object-store.datasource",
                        config.transactionObjectStoreDataSource());
            }
            configuration.put(
                    "quarkus.transaction-manager.object-store.drop-table",
                    String.valueOf(config.transactionObjectStoreDropTable()));
            configuration.put(
                    "quarkus.transaction-manager.object-store.table-prefix",
                    config.transactionObjectStoreTablePrefix());
        }
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

    private final Logger LOG = LoggerFactory.getLogger(DataSourceBeanFactory.class);

    private CamelContext camelContext;

    @Inject
    private AgroalDataSource agroalDataSource;

    @Inject
    private List<AgroalDataSource> agroalDataSources;

    @Override
    public void configure() {

        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Set<String> prefixes = ConfigStore.getInstance().readPrefixes(config, "(.+).jdbc\\..*");

        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(AgroalDataSource.class);

        if (!prefixes.isEmpty()) {
            for (String name : prefixes) {
                if (camelContext.getRegistry().lookupByNameAndType(name, DataSource.class) == null) {
                    DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                    createAggregationRepository(dsFactoryConfig, agroalDataSource);
                }
            }
        } else {
            try {
                if (camelContext.getRegistry().lookupByNameAndType("dataSource", DataSource.class) == null) {
                    final List<ServiceLoader.Provider<DataSourceProvider>> providers =
                            findProviders(DataSourceProvider.class);
                    if (providers.size() == 1) {
                        createAggregationRepository(config, agroalDataSource);
                    } else {
                        throw new IllegalArgumentException("No dataSource implementation is present in the classpath");
                    }
                }
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    private void createAggregationRepository(DataSourceFactoryConfig dsFactoryConfig, DataSource agroalDataSource) {
        if (!dsFactoryConfig.transactionEnabled() && dsFactoryConfig.aggregationRepositoryName() != null) {
            LOG.warn("Transactions have to be enabled in order to create aggregation repositories");
            return;
        }
        if (dsFactoryConfig.aggregationRepositoryName() != null) {
            camelContext
                    .getRegistry()
                    .bind(
                            dsFactoryConfig.aggregationRepositoryName(),
                            new ForageAggregationRepository(
                                    agroalDataSource,
                                    com.arjuna.ats.jta.TransactionManager.transactionManager(),
                                    dsFactoryConfig));
        }
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }
}
