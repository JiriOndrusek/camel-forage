package org.apache.camel.forage.plugin.jms;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.Config;
import org.apache.camel.forage.jms.common.ConnectionFactoryConfig;
import org.apache.camel.forage.jms.common.ConnectionFactoryConfigEntries;
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
public class JmsExportCustomizer extends AbstractExportCustomizer {
    private static final Logger LOG = LoggerFactory.getLogger(JmsExportCustomizer.class);

    @Override
    protected final Config getConfig(String prefix) {
        return new ConnectionFactoryConfig(prefix);
    }

    @Override
    protected final String getPrefix() {
        return "jms";
    }

    @Override
    protected final String getDependencies(RuntimeType runtime) {

        // read all values of jmsKind
        Set<String> jmsKinds = readValuesOfProperty(ConnectionFactoryConfigEntries.JMS_KIND);

        String s = ExportHelper.getDependencies(runtime, ExportHelper.ResourceType.jms) + ","
                + jmsKinds.stream()
                        .map(jmsKind -> ExportHelper.getString(
                                        runtime.name() + ".jmsKind",
                                        ExportHelper.ResourceType.jms,
                                        "Internal error: can not resolve dependencies for %s (%s), runtime: %s.")
                                .replaceAll("\\$\\{jmsKind}", jmsKind))
                        .collect(Collectors.joining(","));

        System.out.println("Using " + s + " dependencies for " + runtime);
        return s;
    }
}
