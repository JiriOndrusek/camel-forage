package org.apache.camel.forage.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.Config;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.logging.log4j.util.Strings;
import org.jline.utils.Log;

public abstract class AbstractExportCustomizer implements ExportCustomizer {

    private Boolean enabled;

    protected abstract String getPrefix();

    //    abstract String getRequiredProperty();

    protected abstract Config getConfig();

    protected abstract String runtimeRelatedDependencies(RuntimeType runtime);

    protected abstract String customDependencies(RuntimeType runtime);

    @Override
    public boolean isEnabled() {
        if (enabled == null) {
            // to enable customizer:
            // - at least one property with required prefix has to exist or such property with prefixed with "name"
            // - todo required property has to be present
            String defaultPropertiesRegexp = "(" + getPrefix() + ")..+";
            Set<String> defaultProperties =
                    ConfigStore.getInstance().readPrefixes(getConfig(), defaultPropertiesRegexp);
            Set<String> prefixes = getPrefixes();

            if (defaultProperties.isEmpty() && prefixes.isEmpty()) {
                Log.warn("No property required for " + getPrefix() + " is present. Configuration is not exported!");
                return enabled = false;
            }

            return enabled = true;
        } else {
            return enabled;
        }
    }

    protected Set<String> getPrefixes() {
        return ConfigStore.getInstance().readPrefixes(getConfig(), "(.+)." + getPrefix() + "..+");
    }

    @Override
    public Set<String> resolveRuntimeDependencies(RuntimeType runtime) {
        System.out.println("**************************************************");
        var runtimeDep = runtimeRelatedDependencies(runtime);
        String[] runtimeArray = runtimeDep == null || runtimeDep.isBlank() ? new String[0] : runtimeDep.split(",");

        var customDep = customDependencies(runtime);
        System.out.println("custom:" + customDep);
        String[] customArray = customDep == null || customDep.isBlank() ? new String[0] : customDep.split(",");

        return Stream.of(runtimeArray, customArray)
                .map(Arrays::asList)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

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
