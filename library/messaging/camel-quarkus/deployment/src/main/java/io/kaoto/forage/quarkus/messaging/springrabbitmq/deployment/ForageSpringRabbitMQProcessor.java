package io.kaoto.forage.quarkus.messaging.springrabbitmq.deployment;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.quarkus.core.deployment.spi.CamelRuntimeBeanBuildItem;
import org.jboss.logging.Logger;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import io.kaoto.forage.core.annotations.FactoryType;
import io.kaoto.forage.core.annotations.FactoryVariant;
import io.kaoto.forage.core.annotations.ForageFactory;
import io.kaoto.forage.core.util.config.ConfigHelper;
import io.kaoto.forage.core.util.config.ConfigStore;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQConfig;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQConstants;
import io.kaoto.forage.quarkus.messaging.springrabbitmq.ForageSpringRabbitMQRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;

/**
 * Quarkus deployment processor for Forage Spring RabbitMQ.
 *
 * <p>Discovers named and default RabbitMQ connection configurations at build time
 * and registers CachingConnectionFactory beans via the recorder.
 *
 * @since 1.4
 */
@ForageFactory(
        value = "Spring RabbitMQ Connection (Quarkus)",
        components = {"camel-spring-rabbitmq"},
        description = "Native Spring RabbitMQ CachingConnectionFactory for Quarkus with compile-time optimization",
        type = FactoryType.SPRING_RABBITMQ_CONNECTION_FACTORY,
        autowired = true,
        configClass = SpringRabbitMQConfig.class,
        variant = FactoryVariant.QUARKUS,
        runtimeDependencies = {"mvn:org.apache.camel.quarkus:camel-quarkus-spring-rabbitmq"})
public class ForageSpringRabbitMQProcessor {

    private static final Logger LOG = Logger.getLogger(ForageSpringRabbitMQProcessor.class);
    private static final String FEATURE = "forage-spring-rabbitmq";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(value = ExecutionTime.STATIC_INIT)
    void registerRabbitConnectionFactories(
            ForageSpringRabbitMQRecorder recorder, BuildProducer<CamelRuntimeBeanBuildItem> beans) {

        SpringRabbitMQConfig defaultConfig = new SpringRabbitMQConfig();
        Set<String> named = ConfigStore.getInstance()
                .readPrefixes(
                        defaultConfig, ConfigHelper.getNamedPropertyRegexp(SpringRabbitMQConstants.MODULE_PREFIX));

        Map<String, SpringRabbitMQConfig> configs;
        if (!named.isEmpty()) {
            configs = named.stream().collect(Collectors.toMap(n -> n, n -> new SpringRabbitMQConfig(n)));
        } else {
            // Check if default (unprefixed) properties exist before creating a default config
            Set<String> defaultPrefixes = ConfigStore.getInstance()
                    .readPrefixes(
                            defaultConfig,
                            ConfigHelper.getDefaultPropertyRegexp(SpringRabbitMQConstants.MODULE_PREFIX));
            if (!defaultPrefixes.isEmpty()) {
                configs = Collections.singletonMap((String) null, defaultConfig);
            } else {
                LOG.debug("No Forage Spring RabbitMQ configuration found, skipping ConnectionFactory discovery");
                return;
            }
        }

        for (Map.Entry<String, SpringRabbitMQConfig> entry : configs.entrySet()) {
            String beanName = entry.getKey() != null ? entry.getKey() : SpringRabbitMQConstants.DEFAULT_BEAN_NAME;
            LOG.infof("Recording Spring RabbitMQ connection factory for bean: %s", beanName);

            RuntimeValue<ConnectionFactory> connectionFactory = recorder.createRabbitConnectionFactory(entry.getKey());
            if (connectionFactory != null) {
                beans.produce(
                        new CamelRuntimeBeanBuildItem(beanName, ConnectionFactory.class.getName(), connectionFactory));
            }
        }
    }
}
