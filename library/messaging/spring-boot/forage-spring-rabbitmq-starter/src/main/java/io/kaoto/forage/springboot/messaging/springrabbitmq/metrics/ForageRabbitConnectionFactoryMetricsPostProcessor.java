package io.kaoto.forage.springboot.messaging.springrabbitmq.metrics;

import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import io.kaoto.forage.messaging.spring.rabbitmq.metrics.RabbitMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import com.rabbitmq.client.ConnectionFactory;

/**
 * {@link BeanPostProcessor} that configures metrics collection on RabbitMQ connection factories.
 *
 * <p>This post processor automatically intercepts all {@link AbstractConnectionFactory} beans
 * and binds RabbitMQ metrics to the application's {@link MeterRegistry}.
 *
 * @since 1.4
 */
class ForageRabbitConnectionFactoryMetricsPostProcessor implements BeanPostProcessor, Ordered {

    private static final String CONNECTION_FACTORY_SUFFIX = "connectionFactory";

    private final ApplicationContext context;

    private volatile MeterRegistry meterRegistry;

    ForageRabbitConnectionFactoryMetricsPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof AbstractConnectionFactory connectionFactory) {
            bindConnectionFactoryToRegistry(getMeterRegistry(), beanName, connectionFactory);
        }
        return bean;
    }

    private void bindConnectionFactoryToRegistry(
            MeterRegistry registry, String beanName, AbstractConnectionFactory connectionFactory) {
        ConnectionFactory rabbitConnectionFactory = connectionFactory.getRabbitConnectionFactory();
        String connectionFactoryName = getConnectionFactoryName(beanName);
        new RabbitMetrics(rabbitConnectionFactory, Tags.of("name", connectionFactoryName)).bindTo(registry);
    }

    private String getConnectionFactoryName(String beanName) {
        if (beanName.length() > CONNECTION_FACTORY_SUFFIX.length()
                && StringUtils.endsWithIgnoreCase(beanName, CONNECTION_FACTORY_SUFFIX)) {
            return beanName.substring(0, beanName.length() - CONNECTION_FACTORY_SUFFIX.length());
        }
        return beanName;
    }

    private MeterRegistry getMeterRegistry() {
        MeterRegistry meterRegistry = this.meterRegistry;
        if (meterRegistry == null) {
            meterRegistry = this.context.getBean(MeterRegistry.class);
            this.meterRegistry = meterRegistry;
        }
        return meterRegistry;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
