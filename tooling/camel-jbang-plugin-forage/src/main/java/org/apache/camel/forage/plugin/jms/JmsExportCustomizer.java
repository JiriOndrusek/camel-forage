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
    protected final Config getConfig() {
        return new ConnectionFactoryConfig();
    }

    @Override
    protected final String getPrefix() {
        return "jms";
    }

    @Override
    protected final String getDependencies(RuntimeType runtime) {
        System.out.println("////////////////////////////////" + runtime);

        // read all values of jmsKind
        Set<String> jmsKinds = readValuesOfProperty(ConnectionFactoryConfigEntries.JMS_KIND);

        // default property

        return switch (runtime) {
            case main -> "todo";
            case springBoot -> "todo";
            case quarkus -> getQuarkusDependencies(jmsKinds);
        };
    }

    private String getQuarkusDependencies(Set<String> jmsKinds) {

        String s = ExportHelper.getDependencies(RuntimeType.quarkus, ExportHelper.ResourceType.jms) + ","
                + jmsKinds.stream()
                        .map(jmsKind -> switch (jmsKind) {
                            case "artemis" -> ExportHelper.getString(
                                    "artemis",
                                    ExportHelper.ResourceType.jms,
                                    "Internal error: can not resolve version of quarkus.artemis.");
                            case "ibm" -> throw new IllegalArgumentException("Ibmmq"); // todo imbmq
                            default -> throw new IllegalArgumentException("Unknown quarkus jms kind: " + jmsKind);
                        })
                        .collect(Collectors.joining(","));

        return s;
    }
}
