package io.kaoto.forage.quarkus.deployment;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.quarkus.ForageQuarkusChatModelsRecorder;
import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import org.apache.camel.quarkus.core.deployment.spi.CamelRuntimeBeanBuildItem;

//
// @ForageFactory(
//        value = "DataSource (Quarkus)",
//        components = {"camel-sql", "camel-jdbc"},
//        description = "Native JDBC DataSource for Quarkus with compile-time optimization and repository support",
//        type = FactoryType.DATA_SOURCE,
//        autowired = true,
//        configClass = DataSourceFactoryConfig.class,
//        variant = FactoryVariant.QUARKUS)
public class ForageQuarkusChatModelsProcessor {

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void quarkusChatModels(
            ForageQuarkusChatModelsRecorder recorder,
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
}
