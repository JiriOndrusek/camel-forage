package io.kaoto.forage.springboot.messaging.springrabbitmq;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import io.kaoto.forage.core.annotations.FactoryType;
import io.kaoto.forage.core.annotations.FactoryVariant;
import io.kaoto.forage.core.annotations.ForageFactory;
import io.kaoto.forage.core.util.config.ConfigHelper;
import io.kaoto.forage.core.util.config.ConfigStore;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQConfig;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQConnectionFactoryHelper;
import io.kaoto.forage.messaging.spring.rabbitmq.common.SpringRabbitMQConstants;
import io.kaoto.forage.springboot.common.SpringPropertyHelper;

/**
 * Auto-configuration for Forage Spring RabbitMQ CachingConnectionFactory creation.
 * Automatically creates ConnectionFactory beans from Spring RabbitMQ configuration properties,
 * supporting both single and multi-instance (prefixed) configurations.
 *
 * <p>Named/prefixed connection factories (e.g., {@code forage.mq1.spring.rabbitmq.host})
 * are registered dynamically by {@link ForageSpringRabbitMQBeanRegistrar}.
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

    private static final Logger LOG = LoggerFactory.getLogger(ForageSpringRabbitMQAutoConfiguration.class);

    /**
     * Registers bean definitions for named/prefixed Spring RabbitMQ connection factories.
     */
    @Bean
    static ForageSpringRabbitMQBeanRegistrar forageSpringRabbitMQBeanRegistrar(Environment environment) {
        return new ForageSpringRabbitMQBeanRegistrar(environment);
    }

    /**
     * Fallback ConnectionFactory bean created when no named/prefixed configurations are found
     * and default (unprefixed) Spring RabbitMQ properties exist.
     */
    @Bean(SpringRabbitMQConstants.DEFAULT_BEAN_NAME)
    @ConditionalOnMissingBean(name = SpringRabbitMQConstants.DEFAULT_BEAN_NAME)
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            prefix = "forage." + SpringRabbitMQConstants.MODULE_PREFIX,
            name = "host")
    public org.springframework.amqp.rabbit.connection.ConnectionFactory forageDefaultRabbitConnectionFactory() {
        SpringRabbitMQConfig config = new SpringRabbitMQConfig();
        LOG.info(
                "Creating default Spring RabbitMQ ConnectionFactory - Host: {}, Port: {}, Username: {}, VirtualHost: {}",
                config.host(),
                config.port(),
                config.username(),
                config.virtualHost());

        org.springframework.amqp.rabbit.connection.CachingConnectionFactory connectionFactory =
                SpringRabbitMQConnectionFactoryHelper.createCachingConnectionFactory(config);

        LOG.info("Registered default Spring RabbitMQ ConnectionFactory bean");
        return connectionFactory;
    }

    /**
     * Bean definition registry post-processor that discovers prefixed Spring RabbitMQ configurations
     * and registers them as ConnectionFactory beans.
     */
    static class ForageSpringRabbitMQBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

        private final Environment environment;

        ForageSpringRabbitMQBeanRegistrar(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            Set<String> prefixes = discoverPrefixes();
            if (!prefixes.isEmpty()) {
                LOG.info("Discovered Forage Spring RabbitMQ configuration prefixes: {}", prefixes);
                registerBeans(registry, prefixes);
            } else {
                LOG.debug("No Forage Spring RabbitMQ prefixed configurations found");
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            // No post-processing needed
        }

        private Set<String> discoverPrefixes() {
            Set<String> prefixes = SpringPropertyHelper.discoverPrefixes(
                    environment, ConfigHelper.getNamedPropertyRegexp(SpringRabbitMQConstants.MODULE_PREFIX));
            if (!prefixes.isEmpty()) {
                return prefixes;
            }
            // Also check ConfigStore (covers forage-*.properties files not loaded into Spring Environment)
            SpringRabbitMQConfig defaultConfig = new SpringRabbitMQConfig();
            return ConfigStore.getInstance()
                    .readPrefixes(
                            defaultConfig, ConfigHelper.getNamedPropertyRegexp(SpringRabbitMQConstants.MODULE_PREFIX));
        }

        private void registerBeans(BeanDefinitionRegistry registry, Set<String> prefixes) {
            LOG.debug("Registering Forage Spring RabbitMQ beans for prefixes: {}", prefixes);
            boolean isFirst = true;
            for (String name : prefixes.stream().sorted().toList()) {
                if (!registry.containsBeanDefinition(name)) {
                    registerConnectionFactoryBean(registry, name, isFirst);
                } else {
                    LOG.debug("Bean '{}' already defined, skipping registration", name);
                }
                isFirst = false;
            }
        }

        private void registerConnectionFactoryBean(BeanDefinitionRegistry registry, String name, boolean isFirst) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(org.springframework.amqp.rabbit.connection.ConnectionFactory.class);
            beanDefinition.setInstanceSupplier(() -> createConnectionFactory(name));
            registry.registerBeanDefinition(name, beanDefinition);
            LOG.info("Registered Spring RabbitMQ ConnectionFactory bean definition: {}", name);

            String defaultName = SpringRabbitMQConstants.DEFAULT_BEAN_NAME;
            if (isFirst && !registry.containsBeanDefinition(defaultName)) {
                GenericBeanDefinition defaultDef = new GenericBeanDefinition();
                defaultDef.setBeanClass(org.springframework.amqp.rabbit.connection.ConnectionFactory.class);
                defaultDef.setInstanceSupplier(() -> createConnectionFactory(name));
                registry.registerBeanDefinition(defaultName, defaultDef);
                LOG.info("Registered default Spring RabbitMQ ConnectionFactory bean definition using: {}", name);
            }
        }

        private org.springframework.amqp.rabbit.connection.ConnectionFactory createConnectionFactory(String name) {
            SpringRabbitMQConfig config = new SpringRabbitMQConfig(name);
            LOG.info(
                    "Creating Spring RabbitMQ ConnectionFactory '{}' - Host: {}, Port: {}, Username: {}, VirtualHost: {}",
                    name,
                    config.host(),
                    config.port(),
                    config.username(),
                    config.virtualHost());

            return SpringRabbitMQConnectionFactoryHelper.createCachingConnectionFactory(config);
        }
    }
}
