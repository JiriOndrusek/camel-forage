package org.apache.camel.forage.plugin.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.common.DataSourceFactoryConfigHelper;

public class ExportQuarkus {

    public static List<String> resolveRuntimeDependencies() {
        List<String> dependencies = new ArrayList<>();
        dependencies.add("mvn:org.apache.camel.forage:forage-quarkus-jdbc-configurer:1.0-SNAPSHOT");

        try {
            DataSourceFactoryConfig config = new DataSourceFactoryConfig();
            Set<String> prefixes =
                    ConfigStore.getInstance().readPrefixes(config, DataSourceFactoryConfigHelper.JDBC_PREFIXES_REGEXP);
            if (!prefixes.isEmpty()) {
                for (String name : prefixes) {
                    DataSourceFactoryConfig dsFactoryConfig = new DataSourceFactoryConfig(name);
                    // todoo get quarkus version
                    dependencies.add(" mvn:io.quarkus:quarkus-jdbc-" + dsFactoryConfig.dbKind() + ":3.26.4");
                }
            } else {
                // todo get quarkus version
                dependencies.add(" mvn:io.quarkus:quarkus-jdbc-" + config.dbKind() + ":3.26.4");
            }

        } catch (Exception e) {
            // todo log error
        }

        return dependencies;
    }
}
