package org.apache.camel.forage.quarkus.jdbc;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.common.aggregation.ForageAggregationRepository;
import org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository;
import org.jboss.logging.Logger;

@Recorder
public class ForageJdbcRecorder {
    private static final org.jboss.logging.Logger LOG = Logger.getLogger(ForageJdbcRecorder.class);

    public List<JdbcAggregationRepository> configureAggregator(RuntimeValue<CamelContext> camelContext) {

        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Set<String> prefixes = ConfigStore.getInstance().readPrefixes(config, "(.+).jdbc\\..*");
        CamelContext context = camelContext.getValue();

        List<JdbcAggregationRepository> aggregators = new ArrayList<>();
        if (!prefixes.isEmpty()) {
            //            for (String name : prefixes) {
            //                if (context.getRegistry().lookupByNameAndType(name, DataSource.class) == null) {
            //                    DataSource ds = context
            //                    createAggregationRepository(dsFactoryConfig, agroalDataSource);
            //                }
            //            }
        } else {

            DataSource agroalDataSource = context.getRegistry().findSingleByType(DataSource.class);
            JdbcAggregationRepository ar = createAggregationRepository(context, config, agroalDataSource);
            if (ar != null) {
                aggregators.add(ar);
            }
            //            try {
            //                if (context.getRegistry().lookupByNameAndType("dataSource", DataSource.class) == null) {
            //                    final List<ServiceLoader.Provider<DataSourceProvider>> providers =
            //                            findProviders(DataSourceProvider.class);
            //                    if (providers.size() == 1) {
            //                        createAggregationRepository(config, agroalDataSource);
            //                    } else {
            //                        throw new IllegalArgumentException("No dataSource implementation is present in the
            // classpath");
            //                    }
            //                }
            //            } catch (Exception ex) {
            //                LOG.error(ex.getMessage(), ex);
            //            }

        }

        return aggregators;
    }

    private JdbcAggregationRepository createAggregationRepository(
            CamelContext camelContext, DataSourceFactoryConfig dsFactoryConfig, DataSource agroalDataSource) {
        if (!dsFactoryConfig.transactionEnabled() && dsFactoryConfig.aggregationRepositoryName() != null) {
            LOG.warn("Transactions have to be enabled in order to create aggregation repositories");
            return null;
        }
        if (dsFactoryConfig.aggregationRepositoryName() != null) {
            return new ForageAggregationRepository(
                    agroalDataSource, com.arjuna.ats.jta.TransactionManager.transactionManager(), dsFactoryConfig);
        }
        return null;
    }
}
