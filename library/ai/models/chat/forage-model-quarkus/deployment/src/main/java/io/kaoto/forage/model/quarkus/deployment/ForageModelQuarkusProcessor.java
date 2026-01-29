package io.kaoto.forage.model.quarkus.deployment;

import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.core.ai.ModelProvider;
import io.kaoto.forage.model.quarkus.runtime.ForageModelQuarkusRecorder;
import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import org.apache.camel.quarkus.core.deployment.spi.CamelRuntimeBeanBuildItem;

class ForageModelQuarkusProcessor {

    private static final String FEATURE = "forage-model-quarkus";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void quarkusChatModels(
            ForageModelQuarkusRecorder recorder,
            BuildProducer<CamelRuntimeBeanBuildItem> camelRuntimeBean,
            BeanDiscoveryFinishedBuildItem discovery) {

        discovery
                .beanStream()
                .filter(bean -> bean.getTypes().contains(ChatModel.class))
                .forEach(bean -> {
                    camelRuntimeBean.produce(new CamelRuntimeBeanBuildItem(
                            bean.getName() + "ForageModelProvider",
                            ModelProvider.class.getName(),
                            recorder.createModelProvider(bean.getName())));
                });
    }

    @BuildStep
    IndexDependencyBuildItem registerDependencyForIndex() {
        return new IndexDependencyBuildItem("io.kaoto.forage", "forage-agent");
    }
}
