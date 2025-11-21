package org.apache.camel.forage.plugin.datasource;

import org.apache.camel.forage.core.common.RuntimeType;
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
    public boolean isEnabled() {
        return true;
    }

    @Override
    String runtimeRelatedDependencies(RuntimeType runtime) {
        System.out.println("//////////////////////////////////////");
        System.out.println("ExportHelper.getDependencies(ExportHelper.DependenciesType.quarkus_jms)");
        return ExportHelper.getDependencies(ExportHelper.DependenciesType.quarkus_jms);
    }

    @Override
    String customDependencies(RuntimeType runtime) {
        return "mvn:io.quarkiverse.artemis:quarkus-artemis-jms:"
                + ExportHelper.getString("quarkus.artemis.jms.version", "Unknown quarkus artemis version");
    }
}
