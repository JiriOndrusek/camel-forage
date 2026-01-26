package io.kaoto.forage.quarkus.jdbc.deployment;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import io.kaoto.forage.quarkus.jdbc.ForageQuarkusChatModelsRecorder;
import io.kaoto.forage.quarkus.jdbc.QuarkusModelProvider;
import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
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
public class ForageQuarkusChatModelsJdbcProcessor {

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    ServiceProviderBuildItem quarkusChatModels(
            ForageQuarkusChatModelsRecorder recorder,
            BuildProducer<CamelRuntimeBeanBuildItem> camelRuntimeBean,
            BeanDiscoveryFinishedBuildItem discovery) {

        //        System.out.println("**************************************************");
        //        System.out.println("********* registering: " + "ollama" + "**************");
        //        System.out.println("**************************************************");
        //        camelRuntimeBean.produce(new CamelRuntimeBeanBuildItem(
        //                "ollameForageModelProvider", ModelProvider.class.getName(),
        // recorder.createModelProvider("ollama")));

        discovery
                .beanStream()
                .filter(bean -> bean.getTypes().contains(ChatModel.class))
                .forEach(bean -> {
                    System.out.println("**************************************************");
                    System.out.println("********* registering: " + bean + "**************");
                    System.out.println("**************************************************");
                    camelRuntimeBean.produce(new CamelRuntimeBeanBuildItem(
                            bean.getName() + "ForageModelProvider",
                            ModelProvider.class.getName(),
                            recorder.createModelProvider(bean.getName())));
                });

        return new ServiceProviderBuildItem(
                "io.kaoto.forage.core.ai.ModelProvider", QuarkusModelProvider.class.getSimpleName());
    }
}
