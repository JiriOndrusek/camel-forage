package io.kaoto.forage.springboot.messaging.springrabbitmq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import io.kaoto.forage.core.annotations.FactoryType;
import io.kaoto.forage.core.annotations.FactoryVariant;
import io.kaoto.forage.core.annotations.ForageFactory;
import io.kaoto.forage.core.messaging.SpringRabbitMQConnectionFactoryProvider;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQConfig;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQModuleDescriptor;
import io.kaoto.forage.springboot.common.ForageSpringBootModuleAdapter;

/**
 * Auto-configuration for Forage Spring RabbitMQ CachingConnectionFactory creation.
 * Automatically creates ConnectionFactory beans from Spring RabbitMQ configuration properties,
 * supporting both single and multi-instance (prefixed) configurations.
 *
 * <p>Both default (unprefixed) and named/prefixed connection factories are registered
 * dynamically by {@link ForageSpringBootModuleAdapter} using the {@link SpringRabbitMQModuleDescriptor}.
 *
 * @since 1.4
 */
@ForageFactory(
        value = "Spring RabbitMQ Connection (Spring Boot)",
        components = {"camel-spring-rabbitmq"},
        description = "Auto-configured Spring RabbitMQ CachingConnectionFactory for Spring Boot",
        type = FactoryType.SPRING_RABBITMQ_CONNECTION_FACTORY,
        autowired = true,
        configClass = SpringRabbitMQConfig.class,
        variant = FactoryVariant.SPRING_BOOT)
@AutoConfiguration(before = RabbitAutoConfiguration.class)
public class ForageSpringRabbitMQAutoConfiguration {

    /**
     * Registers the generic module adapter that discovers both default and prefixed
     * Spring RabbitMQ configurations and registers them as proper bean definitions
     * using the {@link SpringRabbitMQModuleDescriptor}.
     */
    @Bean
    static ForageSpringBootModuleAdapter<SpringRabbitMQConfig, SpringRabbitMQConnectionFactoryProvider>
            forageSpringRabbitMQModuleAdapter(Environment environment) {
        return new ForageSpringBootModuleAdapter<>(new SpringRabbitMQModuleDescriptor(), environment);
    }
}
