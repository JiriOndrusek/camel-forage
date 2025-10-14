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

import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

public class CamelJBangIT extends TestNGCitrusSupport implements TestActionSupport {

    //todo start container

    @Test
    @CitrusTest(name = "RunIntegration_Resource_IT")
    public void singleIT() {

        when(camel().jbang()
                .custom("forage run")
                .integration(Resources.fromClasspath("route.camel.yaml", CamelJBangIT.class))
                .addResource(Resources.fromClasspath("forage-datasource-factory.properties", CamelJBangIT.class))
                .withArg("--runtime", "quarkus"));

        then(camel().jbang().verify().integration("route-2362").waitForLogMessage("HELLO CAMEL #10"));
    }
}
