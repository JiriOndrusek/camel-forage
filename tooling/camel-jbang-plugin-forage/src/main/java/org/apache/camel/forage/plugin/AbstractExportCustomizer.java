package org.apache.camel.forage.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.Config;
import org.apache.camel.forage.core.util.config.ConfigModule;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.logging.log4j.util.Strings;
import org.jline.utils.Log;

public abstract class AbstractExportCustomizer implements ExportCustomizer {

    private Boolean enabled;

    protected abstract String getPrefix();

    protected abstract <T extends Config> T getConfig(String prefix);

    protected abstract String getDependencies(RuntimeType runtime);

    <T extends Config> T getConfig() {
        return getConfig(null);
    }
    ;

    @Override
    public boolean isEnabled() {
        if (enabled == null) {
            // to enable customizer:
            // - at least one property with required prefix has to exist or such property with prefixed with "name"
            // - todo required property has to be present
            String defaultPropertiesRegexp = "(" + getPrefix() + ")..+";
            Set<String> defaultProperties =
                    ConfigStore.getInstance().readPrefixes(getConfig(), defaultPropertiesRegexp);
            Set<String> prefixes = ConfigStore.getInstance().readPrefixes(getConfig(), "(.+)." + getPrefix() + "..+");

            if (defaultProperties.isEmpty() && prefixes.isEmpty()) {
                Log.warn("No property required for " + getPrefix() + " is present. Configuration is not exported!");
                return enabled = false;
            }

            return enabled = true;
        } else {
            return enabled;
        }
    }

    protected Set<String> readValuesOfProperty(ConfigModule entry) {

        Set<String> named = ConfigStore.getInstance().readPrefixes(getConfig(), "(.+)." + getPrefix() + "..+");
        // values from default properties
        Set<Optional<String>> values = new LinkedHashSet<>(named.stream()
                .map(n -> {
                    // read prefixed config
                    getConfig(n);
                    return ConfigStore.getInstance().get(entry.asNamed(n));
                })
                .collect(Collectors.toSet()));
        // add default value
        values.add(ConfigStore.getInstance().get(entry));

        return values.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    @Override
    public Set<String> resolveRuntimeDependencies(RuntimeType runtime) {
        System.out.println("**************************************************");
        System.out.println("Runtime: " + runtime);
        var runtimeDep = getDependencies(runtime);
        String[] runtimeArray = runtimeDep == null || runtimeDep.isBlank() ? new String[0] : runtimeDep.split(",");

        return Stream.of(runtimeArray, runtimeArray)
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
