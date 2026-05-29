package io.kaoto.forage.springboot.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import io.kaoto.forage.core.annotations.FactoryType;
import io.kaoto.forage.core.annotations.FactoryVariant;
import io.kaoto.forage.core.annotations.ForageFactory;
import io.kaoto.forage.core.jms.ConnectionFactoryProvider;
import io.kaoto.forage.jms.common.ConnectionFactoryConfig;
import io.kaoto.forage.jms.common.JmsModuleDescriptor;
import io.kaoto.forage.springboot.common.ForageSpringBootModuleAdapter;

/**
 * Auto-configuration for Forage JMS ConnectionFactory creation using ServiceLoader discovery.
 * Automatically creates ConnectionFactory beans from JMS configuration properties,
 * supporting both single and multi-instance (prefixed) configurations.
 *
 * <p>Both default (unprefixed) and named/prefixed connection factories are registered
 * dynamically by {@link ForageSpringBootModuleAdapter} using the {@link JmsModuleDescriptor}.
 */
@ForageFactory(
        value = "JMS Connection (Spring Boot)",
        components = {"camel-jms"},
        description = "Auto-configured JMS ConnectionFactory for Spring Boot with transaction management",
        type = FactoryType.CONNECTION_FACTORY,
        autowired = true,
        configClass = ConnectionFactoryConfig.class,
        variant = FactoryVariant.SPRING_BOOT)
@Configuration
@AutoConfigureBefore(ArtemisAutoConfiguration.class)
public class ForageConnectionFactoryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ForageConnectionFactoryAutoConfiguration.class);

    /**
     * Transaction management configuration that enables Spring transaction support
     * when JMS transactions are configured.
     */
    @Configuration
    @ConditionalOnProperty(value = "forage.jms.transaction.enabled", havingValue = "true")
    @EnableTransactionManagement
    static class ForageTransactionManagement {

        @jakarta.annotation.PostConstruct
        public void init() {
            log.info("ForageTransactionManagement configuration enabled");
        }
    }

    /**
     * Registers the generic module adapter that discovers both default and prefixed
     * ConnectionFactory configurations and registers them as proper bean definitions
     * using the {@link JmsModuleDescriptor}.
     *
     * <p>The adapter handles provider selection based on {@code forage.jms.kind},
     * supporting multiple JMS providers (Artemis, IBM MQ, etc.) on the classpath.
     */
    @Bean
    static ForageSpringBootModuleAdapter<ConnectionFactoryConfig, ConnectionFactoryProvider> forageJmsModuleAdapter(
            Environment environment) {
        return new ForageSpringBootModuleAdapter<>(new JmsModuleDescriptor(), environment);
    }
}
