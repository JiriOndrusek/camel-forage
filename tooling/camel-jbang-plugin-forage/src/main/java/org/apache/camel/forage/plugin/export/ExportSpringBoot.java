package org.apache.camel.forage.plugin.export;

import java.util.ArrayList;
import java.util.List;

public class ExportSpringBoot {

    public static List<String> resolveRuntimeDependencies() {
        List<String> dependencies = new ArrayList<>();

        //
        //        camel export forage-multi-datasource-factory.properties \
        //        route.camel.yaml \
        //        forage-datasource-factory.properties \
        //        --dep=mvn:org.apache.camel.forage:forage-jdbc-starter:1.0-SNAPSHOT \
        //        --dep=mvn:org.apache.camel.forage:forage-jdbc-postgres:1.0-SNAPSHOT \
        //        --dep=mvn:org.apache.camel.forage:forage-jdbc-mysql:1.0-SNAPSHOT \
        //        --runtime=spring-boot \
        //        --gav=com.foo:acme:1.0-SNAPSHOT

        return dependencies;
    }
}
