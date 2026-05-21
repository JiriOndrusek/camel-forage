package io.kaoto.forage.messaging.spring.rabbitmq.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import io.kaoto.forage.core.common.BeanProvider;

/**
 * Provider for Spring RabbitMQ CachingConnectionFactory beans.
 *
 * <p>This provider creates fully configured {@link CachingConnectionFactory} instances
 * based on Forage configuration properties. Unlike JMS/JDBC which have multiple provider
 * implementations (artemis, ibmmq, postgresql, mysql), Spring RabbitMQ has only one
 * provider since all configurations use the same CachingConnectionFactory type.
 *
 * @since 1.4
 */
public class SpringRabbitMQConnectionFactoryProvider
        implements BeanProvider<org.springframework.amqp.rabbit.connection.ConnectionFactory> {

    private static final Logger LOG = LoggerFactory.getLogger(SpringRabbitMQConnectionFactoryProvider.class);

    @Override
    public org.springframework.amqp.rabbit.connection.ConnectionFactory create(String id) {
        SpringRabbitMQConfig config = id == null ? new SpringRabbitMQConfig() : new SpringRabbitMQConfig(id);

        // Validate required properties
        if (config.host() == null || config.host().trim().isEmpty()) {
            throw new IllegalStateException(
                    "Spring RabbitMQ host is required but not configured" + (id != null ? " for: " + id : ""));
        }

        LOG.info(
                "Creating Spring RabbitMQ ConnectionFactory{} - Host: {}, Port: {}, Username: {}, VirtualHost: {}, "
                        + "ChannelCacheSize: {}, CacheMode: {}",
                id != null ? " '" + id + "'" : "",
                config.host(),
                config.port(),
                config.username(),
                config.virtualHost(),
                config.channelCacheSize(),
                config.cacheMode());

        CachingConnectionFactory connectionFactory =
                SpringRabbitMQConnectionFactoryHelper.createCachingConnectionFactory(config);

        LOG.info("Spring RabbitMQ ConnectionFactory{} created successfully", id != null ? " '" + id + "'" : "");
        return connectionFactory;
    }
}
