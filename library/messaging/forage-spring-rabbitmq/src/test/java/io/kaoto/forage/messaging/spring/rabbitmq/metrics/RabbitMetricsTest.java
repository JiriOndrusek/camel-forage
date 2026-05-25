package io.kaoto.forage.messaging.spring.rabbitmq.metrics;

import java.util.Collections;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MetricsCollector;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link RabbitMetrics}.
 */
class RabbitMetricsTest {

    @Test
    void metricsBindsToMeterRegistry() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        RabbitMetrics rabbitMetrics = new RabbitMetrics(connectionFactory, Collections.emptyList());
        rabbitMetrics.bindTo(meterRegistry);

        assertThat(connectionFactory.getMetricsCollector()).isNotNull();
    }

    @Test
    void metricsBindsWithTags() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        Iterable<Tag> tags = Collections.singletonList(Tag.of("name", "testConnection"));

        RabbitMetrics rabbitMetrics = new RabbitMetrics(connectionFactory, tags);
        rabbitMetrics.bindTo(meterRegistry);

        assertThat(connectionFactory.getMetricsCollector()).isNotNull();
    }

    @Test
    void metricsBindsWithNullTags() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        RabbitMetrics rabbitMetrics = new RabbitMetrics(connectionFactory, null);
        rabbitMetrics.bindTo(meterRegistry);

        assertThat(connectionFactory.getMetricsCollector()).isNotNull();
    }

    @Test
    void constructorThrowsExceptionWhenConnectionFactoryIsNull() {
        assertThatThrownBy(() -> new RabbitMetrics(null, Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'connectionFactory' must not be null");
    }

    @Test
    void metricsCollectorIsConfigured() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        RabbitMetrics rabbitMetrics = new RabbitMetrics(connectionFactory, Collections.emptyList());
        rabbitMetrics.bindTo(meterRegistry);

        verify(connectionFactory).setMetricsCollector(any(MetricsCollector.class));
    }

    @Test
    void metricsDoesNotReplaceExistingCollector() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        // Set an existing metrics collector
        MetricsCollector existingCollector = mock(MetricsCollector.class);
        connectionFactory.setMetricsCollector(existingCollector);

        // Try to bind metrics - should not replace existing collector
        RabbitMetrics rabbitMetrics = new RabbitMetrics(connectionFactory, Collections.emptyList());
        rabbitMetrics.bindTo(meterRegistry);

        // Verify the existing collector is still in place
        assertThat(connectionFactory.getMetricsCollector()).isSameAs(existingCollector);
    }
}
