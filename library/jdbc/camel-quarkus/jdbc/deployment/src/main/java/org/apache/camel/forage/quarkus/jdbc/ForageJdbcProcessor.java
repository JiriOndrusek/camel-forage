/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.forage.quarkus.jdbc;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;
import java.util.List;
import org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository;
import org.apache.camel.quarkus.core.deployment.spi.CamelContextBuildItem;
import org.apache.camel.quarkus.core.deployment.spi.CamelRuntimeBeanBuildItem;
import org.jboss.logging.Logger;

class ForageJdbcProcessor {

    private static final Logger LOG = Logger.getLogger(ForageJdbcProcessor.class);
    private static final String FEATURE = "camel-forage-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(value = ExecutionTime.STATIC_INIT)
    void registerAggregation(
            CamelContextBuildItem context, ForageJdbcRecorder recorder, BuildProducer<CamelRuntimeBeanBuildItem> beans)
            throws Exception {
        List<JdbcAggregationRepository> aggregationRepositoryList =
                recorder.configureAggregator(context.getCamelContext());
        aggregationRepositoryList.forEach(r -> beans.produce(new CamelRuntimeBeanBuildItem(
                r.getRepositoryName(), JdbcAggregationRepository.class.getSimpleName(), new RuntimeValue<>(r))));
    }
}
