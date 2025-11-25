package org.apache.camel.forage.plugin.datasource;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfigEntries;
import org.apache.camel.forage.plugin.AbstractExportCustomizer;
import org.apache.camel.forage.plugin.ExportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of export customizer for datasource properties.
 *
 * <p>
 * Adds quarkus or spring-boot runtime dependencies, thus making export command less verbose.
 * </p>
 */
public class DatasourceExportCustomizer extends AbstractExportCustomizer {
    private static final Logger LOG = LoggerFactory.getLogger(DatasourceExportCustomizer.class);

    @Override
    protected final DataSourceFactoryConfig getConfig(String prefix) {
        return new DataSourceFactoryConfig(prefix);
    }

    @Override
    protected final String getPrefix() {
        return "jdbc";
    }

    @Override
    protected final String getDependencies(RuntimeType runtime) {

        // read all values of jmsKind
        Set<String> dbKinds = readValuesOfProperty(DataSourceFactoryConfigEntries.DB_KIND);

        // default property

        String dependencies = ExportHelper.getDependencies(runtime, ExportHelper.ResourceType.datasource) + ","
                + dbKinds.stream()
                        .map(dbKind -> ExportHelper.getString(
                                        runtime.name() + ".dbKind",
                                        ExportHelper.ResourceType.datasource,
                                        "Internal error: can not resolve dependencies for %s (%s), runtime: %s."
                                                .formatted(ExportHelper.ResourceType.datasource, dbKind, runtime))
                                .replaceAll("\\$\\{dbKind}", dbKind))
                        .collect(Collectors.joining(","));

        // todo better location +  add named
        if (runtime == RuntimeType.quarkus && getConfig(null).transactionEnabled()) {
            dependencies += "," + "mvn:io.quarkus:quarkus-narayana-jta:" + ExportHelper.getQuarkusVersion();
        }

        System.out.println("Using " + dependencies + " dependencies for " + runtime);
        return dependencies;
    }
}
