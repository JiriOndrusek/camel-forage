package org.apache.camel.forage.plugin.datasource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.camel.forage.plugin.ExportHelper;
import org.apache.logging.log4j.util.Strings;

public abstract class AbstractExportCustomizer implements ExportCustomizer {

    @Override
    public abstract boolean isEnabled();

    @Override
    public Set<String> resolveRuntimeDependencies(RuntimeType runtime) {
        var runtimeDep = customDependencies(runtime);
        String[] runtimeArray = runtimeDep == null || runtimeDep.isBlank() ? new String[0] : runtimeDep.split(",");

        var customDep = customDependencies(runtime);
        String[] customArray = customDep == null || customDep.isBlank() ? new String[0] : customDep.split(",");

        return Stream.of(runtimeArray, customArray)
                .map(Arrays::asList)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    abstract String runtimeRelatedDependencies(RuntimeType runtime);

    abstract String customDependencies(RuntimeType runtime);

    private static void listDependencies(
            Set<String> dependencies,
            String basicDependencies,
            String depPrefix,
            String depVersion,
            RuntimeType runtime) {
        dependencies.addAll(Arrays.asList(basicDependencies.split(",")));

        try {
            DataSourceFactoryConfig config = new DataSourceFactoryConfig();
            Set<String> prefixes = ConfigStore.getInstance().readPrefixes(config, ExportHelper.JDBC_PREFIXES_REGEXP);

            if (!prefixes.isEmpty()) {
                for (String name : prefixes) {
                    DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                    // todo get quarkus version
                    dependencies.add(depPrefix + dsFactoryConfig.dbKind() + depVersion);
                }
            } else {
                // logs a warn message, how to skip it?
                if (Strings.isNotBlank(config.dbKind())) {
                    dependencies.add(depPrefix + config.dbKind() + depVersion);
                }
            }

            // todo better location
            if (runtime == RuntimeType.quarkus && config.transactionEnabled()) {
                dependencies.add("mvn:io.quarkus:quarkus-narayana-jta:" + ExportHelper.getQuarkusVersion());
            }
        } catch (Exception ex) {
            // todo log error
        }
    }
}
