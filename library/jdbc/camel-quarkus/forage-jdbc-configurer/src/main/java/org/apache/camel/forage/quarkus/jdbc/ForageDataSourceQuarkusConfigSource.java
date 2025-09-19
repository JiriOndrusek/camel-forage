package org.apache.camel.forage.quarkus.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.camel.forage.core.jdbc.DataSourceProvider;
import org.apache.camel.forage.jdbc.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.MultiDataSourceConfig;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Configuration
// @AutoConfigureBefore({DataSourceAutoConfiguration.class, AgroalDataSourceAutoConfiguration.class})
public class ForageDataSourceQuarkusConfigSource implements ConfigSource {

    private static final Map<String, String> configuration = new HashMap<>();

    private boolean initialized = false;

    //    static {
    //        configuration.put("quarkus.datasource.db-kind", "postgresql");
    //        configuration.put("quarkus.datasource.password", "test");
    //        configuration.put("quarkus.datasource.username", "test");
    //        configuration.put("quarkus.datasource.jdbc.url", "jdbc:postgresql://localhost:5432/postgresql");
    //        configuration.put("quarkus.datasource.jdbc.max-size", "16");
    //    }

    @Override
    public int getOrdinal() {
        return 275;
    }

    @Override
    public Set<String> getPropertyNames() {
        initialize();
        return configuration.keySet();
    }

    @Override
    public String getValue(final String propertyName) {
        initialize();
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return ForageDataSourceQuarkusConfigSource.class.getSimpleName();
    }

    private final Logger LOG = LoggerFactory.getLogger(ForageDataSourceQuarkusConfigSource.class);

    private final MultiDataSourceConfig config = new MultiDataSourceConfig();
    private static final String DEFAULT_DATASOURCE = "dataSource";

    private void initialize() {
        if (initialized) {
            return;
        }

        if (config.multiDataSourceNames() != null
                && !config.multiDataSourceNames().isEmpty()) {
            for (String name : config.multiDataSourceNames()) {
                DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                DataSource agroalDataSource = newDataSource(dsFactoryConfig, name);
            }
        } else {
            // todo how to get provides???
            //            try {
            //                    final List<ServiceLoader.Provider<DataSourceProvider>> providers =
            //                            ServiceLoaderHelper.findProviders(DataSourceProvider.class);
            //                    if (providers.size() == 1) {
            //                        DataSource agroalDataSource = providers.get(0).get().create();
            //                        camelContext.getRegistry().bind(DEFAULT_DATASOURCE, agroalDataSource);
            //                    } else {
            //                        throw new IllegalArgumentException("No dataSource implementation is present in the
            // classpath");
            //                    }
            //                }
            //            } catch (Exception ex) {
            //                LOG.debug(ex.getMessage(), ex);
            //            }
        }
    }

    private synchronized DataSource newDataSource(DataSourceFactoryConfig dataSourceFactoryConfig, String name) {
        final String dataSourceProviderClass = dataSourceFactoryConfig.providerDataSourceClass();
        LOG.info("Configuring DataSource of type {}", dataSourceProviderClass);

        //        final List<ServiceLoader.Provider<DataSourceProvider>> providers =
        // ServiceLoaderHelper.findProviders(DataSourceProvider.class);

        //        final ServiceLoader.Provider<DataSourceProvider> dataSourceProvider =
        //                ServiceLoaderHelper.findProviderByClassName(providers, dataSourceProviderClass);
        //
        //        if (dataSourceProvider == null) {
        //            LOG.warn("DataSource {} has no provider for {}", name, dataSourceProviderClass);
        //            return null;
        //        }

        return doCreateDataSource(null, name);
    }

    private DataSource doCreateDataSource(ServiceLoader.Provider<DataSourceProvider> provider, String name) {
        final DataSourceProvider dataSourceProvider = provider.get();
        return dataSourceProvider.create(name);
    }
}
