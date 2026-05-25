package io.kaoto.forage.messaging.spring.rabbitmq.metrics;

import java.util.Collections;
import org.springframework.util.Assert;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.MicrometerMetricsCollector;

/**
 * A {@link MeterBinder} for RabbitMQ Java Client metrics.
 *
 * <p>This class integrates RabbitMQ client metrics with Micrometer by configuring
 * a {@link MicrometerMetricsCollector} on the provided {@link ConnectionFactory}.
 *
 * @since 1.4
 */
public class RabbitMetrics implements MeterBinder {

    private final Iterable<Tag> tags;

    private final ConnectionFactory connectionFactory;

    /**
     * Create a new meter binder recording the specified {@link ConnectionFactory}.
     *
     * @param connectionFactory the {@link ConnectionFactory} to instrument
     * @param tags tags to apply to all recorded metrics (can be null)
     */
    public RabbitMetrics(ConnectionFactory connectionFactory, Iterable<Tag> tags) {
        Assert.notNull(connectionFactory, "'connectionFactory' must not be null");
        this.connectionFactory = connectionFactory;
        this.tags = (tags != null) ? tags : Collections.emptyList();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.connectionFactory.setMetricsCollector(new MicrometerMetricsCollector(registry, "rabbitmq", this.tags));
    }
}
