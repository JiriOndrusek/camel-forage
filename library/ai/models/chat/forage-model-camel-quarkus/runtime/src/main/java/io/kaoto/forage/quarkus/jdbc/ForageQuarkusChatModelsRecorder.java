package io.kaoto.forage.quarkus.jdbc;

import io.kaoto.forage.core.ai.ModelProvider;
import io.kaoto.forage.jdbc.common.DataSourceFactoryConfig;
import io.kaoto.forage.jdbc.common.aggregation.ForageAggregationRepository;
import io.kaoto.forage.jdbc.common.idempotent.ForageIdRepository;
import io.kaoto.forage.jdbc.common.idempotent.ForageJdbcMessageIdRepository;
import io.kaoto.forage.jdbc.db2.Db2Jdbc;
import io.kaoto.forage.jdbc.h2.H2Jdbc;
import io.kaoto.forage.jdbc.hsqldb.HsqldbJdbc;
import io.kaoto.forage.jdbc.mariadb.MariadbJdbc;
import io.kaoto.forage.jdbc.mssql.MssqlJdbc;
import io.kaoto.forage.jdbc.mysql.MysqlJdbc;
import io.kaoto.forage.jdbc.oracle.OracleJdbc;
import io.kaoto.forage.jdbc.postgresql.PostgresqlJdbc;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository;
import org.apache.camel.processor.idempotent.jdbc.JdbcMessageIdRepository;
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
