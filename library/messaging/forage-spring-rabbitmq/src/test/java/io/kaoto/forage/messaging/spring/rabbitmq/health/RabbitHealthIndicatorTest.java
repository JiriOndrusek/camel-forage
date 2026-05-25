package io.kaoto.forage.messaging.spring.rabbitmq.health;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RabbitHealthIndicator}.
 */
class RabbitHealthIndicatorTest {

    @Test
    void healthIndicatorReportsUpWhenBrokerIsAvailable() {
        RabbitTemplate rabbitTemplate = createMockRabbitTemplate("3.13.0");
        RabbitHealthIndicator healthIndicator = new RabbitHealthIndicator(rabbitTemplate);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("version", "3.13.0");
    }

    @Test
    void healthIndicatorReportsDownWhenBrokerIsUnavailable() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenThrow(new RuntimeException("Connection refused"));

        RabbitHealthIndicator healthIndicator = new RabbitHealthIndicator(rabbitTemplate);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void healthIndicatorHandlesUnknownVersion() {
        RabbitTemplate rabbitTemplate = createMockRabbitTemplate(null);
        RabbitHealthIndicator healthIndicator = new RabbitHealthIndicator(rabbitTemplate);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("version", "unknown");
    }

    @Test
    void constructorThrowsExceptionWhenRabbitTemplateIsNull() {
        assertThatThrownBy(() -> new RabbitHealthIndicator(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'rabbitTemplate' must not be null");
    }

    private RabbitTemplate createMockRabbitTemplate(String version) {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        Map<String, Object> serverProperties = new HashMap<>();
        if (version != null) {
            serverProperties.put("version", version);
        }

        when(rabbitTemplate.execute(any(ChannelCallback.class))).thenAnswer(invocation -> {
            ChannelCallback<?> callback = invocation.getArgument(0);
            when(channel.getConnection()).thenReturn(connection);
            when(connection.getServerProperties()).thenReturn(serverProperties);
            return callback.doInRabbit(channel);
        });

        return rabbitTemplate;
    }
}
