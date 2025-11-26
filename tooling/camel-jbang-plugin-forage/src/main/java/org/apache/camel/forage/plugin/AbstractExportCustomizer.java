package org.apache.camel.forage.plugin;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.Config;
import org.apache.camel.forage.core.util.config.ConfigModule;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.jline.utils.Log;

public abstract class AbstractExportCustomizer<T extends Config> implements ExportCustomizer {

    private static final String VERIFY_DEFAULT_PROPERTY_REGEXP = "(%s)..+";
    private static final String VERIFY_NAMED_PROPERTY_REGEXP = "(.+).%s..+";

    private Boolean enabled;

    protected abstract String getPrefix();

    protected abstract T getConfig(String prefix);

    protected abstract Set<String> getDependencies(RuntimeType runtime);

    private static String getVerifyDefaultPropertyRegexp(String prefix) {
        return VERIFY_DEFAULT_PROPERTY_REGEXP.formatted(prefix);
    }

    public static String getVerifyNamedPropertyRegexp(String prefix) {
        return VERIFY_NAMED_PROPERTY_REGEXP.formatted(prefix);
    }

    T getConfig() {
        return getConfig(null);
    }

    @Override
    public boolean isEnabled() {
        if (enabled == null) {
            // to enable customizer:
            // - at least one property with required prefix has to exist or such property with prefixed with "name"
            Set<String> defaultProperties =
                    ConfigStore.getInstance().readPrefixes(getConfig(), getVerifyDefaultPropertyRegexp(getPrefix()));
            Set<String> namedProperties =
                    ConfigStore.getInstance().readPrefixes(getConfig(), getVerifyNamedPropertyRegexp(getPrefix()));

            if (defaultProperties.isEmpty() && namedProperties.isEmpty()) {
                Log.warn("No property required for " + getPrefix() + " is present. Configuration is not exported!");
                return enabled = false;
            }

            return enabled = true;
        } else {
            return enabled;
        }
    }

    protected Set<String> readValuesOfProperty(ConfigModule entry) {

        Set<String> named =
                ConfigStore.getInstance().readPrefixes(getConfig(), getVerifyNamedPropertyRegexp(getPrefix()));
        // values from default properties
        Set<Optional<String>> values = named.stream()
                .map(n -> {
                    // read prefixed config
                    getConfig(n);
                    return ConfigStore.getInstance().get(entry.asNamed(n));
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // add default value
        values.add(ConfigStore.getInstance().get(entry));

        return values.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    @Override
    public Set<String> resolveRuntimeDependencies(RuntimeType runtime) {
        return getDependencies(runtime);
    }
}
