package org.apache.camel.forage.plugin.datasource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jms.common.ConnectionFactoryConfig;
import org.apache.camel.forage.plugin.ExportHelper;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of export customizer for datasource properties.
 *
 * <p>
 * Adds quarkus or spring-boot runtime dependencies, thus making export command less verbose.
 * </p>
 */
public class JmsExportCustomizer2 implements ExportCustomizer {
    private static final Logger LOG = LoggerFactory.getLogger(JmsExportCustomizer2.class);

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Set<String> resolveRuntimeDependencies(RuntimeType runtime) {
        Set<String> dependencies = new HashSet<>();

        RuntimeType _runtime = runtime == null ? RuntimeType.main : runtime;

        switch (_runtime) {
            case quarkus -> {
                listDependencies(
                        dependencies,
                        ExportHelper.getDependencies(ExportHelper.DependenciesType.quarkus_jms),
                        "mvn:io.quarkus:quarkus-jms-",
                        ":" + ExportHelper.getQuarkusVersion(),
                        runtime);
            }
            case springBoot -> {
                listDependencies(
                        dependencies,
                        ExportHelper.getDependencies(ExportHelper.DependenciesType.springBoot_jms),
                        "mvn:org.apache.camel.forage:forage-jms-",
                        ":" + ExportHelper.getProjectVersion(),
                        runtime);
            }
            case main -> {
                listDependencies(
                        dependencies,
                        ExportHelper.getDependencies(ExportHelper.DependenciesType.plain_jms),
                        "mvn:org.apache.camel.forage:forage-jms-",
                        ":" + ExportHelper.getProjectVersion(),
                        runtime);
            }
        }

        return dependencies;
    }

    private static void listDependencies(
            Set<String> dependencies,
            String basicDependencies,
            String depPrefix,
            String depVersion,
            RuntimeType runtime) {
        dependencies.addAll(Arrays.asList(basicDependencies.split(",")));

        try {
            ConnectionFactoryConfig config = new ConnectionFactoryConfig();
            Set<String> prefixes = ConfigStore.getInstance().readPrefixes(config, ExportHelper.JMS_PREFIXES_REGEXP);

            if (!prefixes.isEmpty()) {
                for (String name : prefixes) {
                    ConnectionFactoryConfig jmsFactoryConfig = new ConnectionFactoryConfig(name);
                    // todoo get quarkus version
                    dependencies.add(depPrefix + jmsFactoryConfig.jmsKind() + depVersion);
                }
            } else {
                // logs a warn message, how to skip it?
                if (Strings.isNotBlank(config.jmsKind())) {
                    dependencies.add(depPrefix + config.jmsKind() + depVersion);
                }
            }

        } catch (Exception ex) {
            // todo log error
        }
    }
}
