/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.forage.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// @CitrusSupport
@Testcontainers
@ExtendWith(CitrusExtension.class)
public class CamelJBangIT implements TestActionSupport {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withExposedPorts(5432)
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("postgresql")
            .withInitScript("singleITInitScript.sql");

    private static Path tmpDir;

    @BeforeAll
    static void copyResources() throws IOException {
        tmpDir = Paths.get("target/tmp");
        Files.createDirectories(tmpDir);
        Stream.of("forage-datasource-factory.properties", "route.camel.yaml").forEach(resource -> {
            final Path target = tmpDir.resolve(resource);
            if (!Files.exists(target)) {
                try (InputStream in = CamelJBangIT.class.getResourceAsStream(resource)) {
                    Files.copy(in, target);
                } catch (IOException e) {
                    throw new RuntimeException("Could not read resource " + resource, e);
                }
            }
        });
    }

    // todo start container

    @Test
    @CitrusTest(name = "RunIntegration_Resource_IT")
    public void singleIT(@CitrusResource GherkinTestActionRunner runner) {

        String jdbcUrl = String.format("jdbc:postgresql://localhost:%d/postgresql", postgres.getMappedPort(5432));

        runner.when(camel().jbang()
                .custom(Arrays.asList("run"))
                .addResource("route.camel.yaml")
                .addResource("forage-datasource-factory.properties")
                .withArg("--runtime=quarkus")
                .pidName("route")
                .cmdToExecute("forage")
                .integration(Paths.get("target/tmp").toFile().getAbsolutePath())
                .dumpIntegrationOutput(true)
                .withSystemProperties(Map.of("citrus.camel.jbang.version", "4.16.0-SNAPSHOT", "jbc.url", jdbcUrl))
                .withEnvs(Map.of("CITRUS_CAMEL_JBANG_VERSION", "4.16.0-SNAPSHOT", "JDBC_URL", jdbcUrl)));

        runner.then(camel().jbang()
                .verify()
                .integration("route")
                .waitForLogMessage("from jdbc default ds - [{id=1, content=postgres 1}, {id=2, content=postgres 2}]"));
    }
}
