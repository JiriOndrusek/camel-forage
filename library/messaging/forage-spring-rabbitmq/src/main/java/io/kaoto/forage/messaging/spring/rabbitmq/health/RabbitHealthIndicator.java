package io.kaoto.forage.messaging.spring.rabbitmq.health;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.Assert;

/**
 * {@link HealthIndicator} for RabbitMQ messaging system.
 *
 * <p>This health indicator checks the connectivity to RabbitMQ broker by executing
 * a simple operation via {@link RabbitTemplate} and retrieving the server version.
 *
 * @since 1.4
 */
public class RabbitHealthIndicator extends AbstractHealthIndicator {

    private final RabbitTemplate rabbitTemplate;

    public RabbitHealthIndicator(RabbitTemplate rabbitTemplate) {
        super("Rabbit health check failed");
        Assert.notNull(rabbitTemplate, "'rabbitTemplate' must not be null");
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // Check connectivity by retrieving server version
        // If this succeeds, the broker is reachable
        String version = getVersion();
        builder.up().withDetail("version", version);
    }

    private String getVersion() {
        return this.rabbitTemplate.execute((channel) -> {
            Object version = channel.getConnection().getServerProperties().getOrDefault("version", "unknown");
            return version.toString();
        });
    }
}
