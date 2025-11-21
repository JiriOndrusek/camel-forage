package org.apache.camel.forage.plugin.jms;

import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.core.util.config.Config;
import org.apache.camel.forage.jms.common.ConnectionFactoryConfig;
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
    protected final String runtimeRelatedDependencies(RuntimeType runtime) {
        return ExportHelper.getDependencies(ExportHelper.getDependencies("quarkus.jms"));
    }

    @Override
    protected final String customDependencies(RuntimeType runtime) {

        System.out.println("////////////////////////////////" + runtime);
        return switch (runtime) {
            case main -> "todo";
            case springBoot -> "todo";
            case quarkus -> getQuarkusDependencies();
        };
    }

    private String getQuarkusDependencies() {

        // detect jms kinds
        System.out.println("PREFIXES: " + getPrefixes());
        Set<String> jmsKinds = getPrefixes().stream()
                .map(prefix -> new ConnectionFactoryConfig(prefix).jmsKind())
                .collect(Collectors.toSet());

        System.out.println("<DEFAULT>");
        if (jmsKinds.isEmpty()) {
            jmsKinds.add(new ConnectionFactoryConfig().jmsKind());
        }

        return jmsKinds.stream()
                .map(jmsKind -> switch (jmsKind) {
                    case "artemis" -> ExportHelper.getDependencies("quarkus.jms.artemis.dependencies");
                    case "ibm" -> throw new IllegalArgumentException("Ibmmq"); // todo imbmq
                    default -> throw new IllegalArgumentException("Unknown quarkus jms kind: " + jmsKind);
                })
                .collect(Collectors.joining(","));
    }
}
