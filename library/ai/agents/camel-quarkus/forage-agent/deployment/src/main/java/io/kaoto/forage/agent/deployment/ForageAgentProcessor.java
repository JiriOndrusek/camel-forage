package io.kaoto.forage.agent.deployment;

import io.kaoto.forage.agent.runtime.ForageAgentRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.apache.camel.quarkus.core.deployment.spi.CamelContextBuildItem;

// todo @ForageBean
class ForageAgentProcessor {

    private static final String FEATURE = "forage-agent";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(value = ExecutionTime.RUNTIME_INIT)
    void configure(CamelContextBuildItem context, ForageAgentRecorder recorder) {
        recorder.recordAgents(context.getCamelContext());
    }
}
