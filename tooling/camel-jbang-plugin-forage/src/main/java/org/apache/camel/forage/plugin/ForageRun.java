package org.apache.camel.forage.plugin;

import java.util.Set;
import java.util.stream.Stream;
import org.apache.camel.dsl.jbang.core.commands.CamelJBangMain;
import org.apache.camel.dsl.jbang.core.commands.Run;
import org.apache.camel.forage.core.common.ExportCustomizer;
import org.apache.camel.forage.core.common.RuntimeType;
import org.apache.camel.forage.plugin.datasource.DatasourceExportCustomizer;
import org.apache.camel.forage.plugin.datasource.JmsExportCustomizer;

public class ForageRun extends Run {
    public ForageRun(CamelJBangMain main) {
        super(main);
    }

    /**
     * This method is used only for the camel run command with runtime=main
     * All other runtimes extends dependencies by interface {@link org.apache.camel.dsl.jbang.core.common.PluginExporter)
     * from {@link org.apache.camel.forage.plugin.ForagePlugin}
     */
    @Override
    protected void addDependencies(String... deps) {
        super.addDependencies(Stream.of(new DatasourceExportCustomizer(), new JmsExportCustomizer())
                .filter(ExportCustomizer::isEnabled)
                .map(exportCustomizer -> exportCustomizer.resolveRuntimeDependencies(RuntimeType.main))
                .flatMap(Set::stream)
                .distinct()
                .toArray(String[]::new));
    }
}
