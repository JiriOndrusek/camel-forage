package org.apache.camel.forage.jdbc;

import org.apache.camel.forage.integration.tests.IntegrationTestSetupExtension;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

@CitrusSupport
@Testcontainers
@ExtendWith(IntegrationTestSetupExtension.class)
public class AggregationTest implements TestActionSupport {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                    DockerImageName.parse("mirror.gcr.io/postgres:15.0").asCompatibleSubstituteFor("postgres"))
            .withExposedPorts(5432)
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("postgresql")
            .withInitScript("aggregationITInitScript.sql");

    @Test
    @CitrusTest()
    public void aggregationTest(@CitrusResource GherkinTestActionRunner runner) {
        // running jbang forage run with required resources and required runtime
        runner.when(camel().jbang()
                .custom("forage", "run")
                .processName("route")
                .addResource(Resources.fromClasspath(getClass().getSimpleName() + "/event-batching.camel.yaml", getClass()))
                .addResource(Resources.fromClasspath(
                        getClass().getSimpleName() + "/forage-datasource-factory.properties", getClass()))
                .addResource(Resources.fromClasspath(
                        getClass().getSimpleName() + "/MyAggregationStrategy.java", getClass()))
                .dumpIntegrationOutput(true)
                .withArg(System.getProperty(IntegrationTestSetupExtension.RUNTIME_PROPERTY))
                .withEnvs(Collections.singletonMap("JDBC_URL", postgres.getJdbcUrl())));

        runner.when(camel().send().endpoint("direct:events")
                        .fork(true)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<News><Message>Citrus rocks!</Message></News>"));


        // validation of logged message
        runner.then(camel().jbang()
                .verify()
                .integration("route")
                .waitForLogMessage("from jdbc default ds - [{id=1, content=postgres 1}, {id=2, content=postgres 2}]"));
    }
}
