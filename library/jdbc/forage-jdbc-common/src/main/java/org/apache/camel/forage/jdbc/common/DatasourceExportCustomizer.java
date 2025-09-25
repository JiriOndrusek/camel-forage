package org.apache.camel.forage.jdbc.common;

import java.util.HashSet;
import java.util.Set;
import org.apache.camel.dsl.jbang.core.common.RuntimeType;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.util.config.ConfigStore;

public class DatasourceExportCustomizer implements ExportCustomizer {

    @Override
    public Set<String> resolveRuntimeDependencies(RuntimeType runtime) {
        Set<String> dependencies = new HashSet<>();

        switch (runtime) {
            case quarkus -> {
                extracted(
                        dependencies,
                        "mvn:org.apache.camel.forage:forage-quarkus-jdbc-configurer:1.0-SNAPSHOT",
                        "mvn:io.quarkus:quarkus-jdbc-",
                        ":3.26.4",
                        runtime);
            }
            case springBoot -> {
                extracted(
                        dependencies,
                        "mvn:org.apache.camel.forage:forage-jdbc-starter:1.0-SNAPSHOT",
                        "mvn:org.apache.camel.forage:forage-jdbc-",
                        ":1.0-SNAPSHOT",
                        runtime);
            }
        }

        return dependencies;
    }

    private static void extracted(
            Set<String> dependencies, String configurer, String depPrefix, String depVersion, RuntimeType runtime) {
        dependencies.add(configurer);

        try {
            DataSourceFactoryConfig config = new DataSourceFactoryConfig();
            Set<String> prefixes =
                    ConfigStore.getInstance().readPrefixes(config, DataSourceFactoryConfigHelper.JDBC_PREFIXES_REGEXP);

            if (!prefixes.isEmpty()) {
                for (String name : prefixes) {
                    DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                    // todoo get quarkus version
                    dependencies.add(depPrefix
                            + DataSourceFactoryConfigHelper.transformDbKindIntoProviderClass(dsFactoryConfig.dbKind())
                            + depVersion);
                }
            } else {
                // todo get quarkus version
                dependencies.add(depPrefix
                        + DataSourceFactoryConfigHelper.getDbKindNameForRuntime(config.dbKind(), runtime)
                        + depVersion);
            }

        } catch (Exception ex) {
            // todo log error
        }
    }
}
