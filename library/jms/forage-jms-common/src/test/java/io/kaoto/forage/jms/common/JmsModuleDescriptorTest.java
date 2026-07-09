package io.kaoto.forage.jms.common;

import java.util.Map;
import io.kaoto.forage.core.util.config.ConfigStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JmsModuleDescriptorTest {

    private final JmsModuleDescriptor descriptor = new JmsModuleDescriptor();

    @BeforeEach
    void setUp() {
        System.setProperty("forage.jms.kind", "artemis");
        System.setProperty("forage.jms.broker.url", "tcp://localhost:61616");
        ConfigStore.getInstance().reload();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("forage.jms.kind");
        System.clearProperty("forage.jms.broker.url");
        System.clearProperty("forage.jms.pool.block.if.full.timeout.millis");
        System.clearProperty("forage.jms.pool.expiry.timeout.millis");
        System.clearProperty("forage.jms.pool.idle.timeout.millis");
        ConfigStore.getInstance().reload();
    }

    @Test
    void translatePropertiesPreservesNegativeSentinel() {
        System.setProperty("forage.jms.pool.block.if.full.timeout.millis", "-1");
        ConnectionFactoryConfig config = new ConnectionFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        assertThat(props).containsEntry("quarkus.pooled-jms.block-if-session-pool-is-full-timeout", "-1");
    }

    @Test
    void translatePropertiesConvertsMillisToSeconds() {
        System.setProperty("forage.jms.pool.idle.timeout.millis", "60000");
        System.setProperty("forage.jms.pool.block.if.full.timeout.millis", "30000");
        ConnectionFactoryConfig config = new ConnectionFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        assertThat(props).containsEntry("quarkus.pooled-jms.connection-idle-timeout", "60");
        assertThat(props).containsEntry("quarkus.pooled-jms.block-if-session-pool-is-full-timeout", "30");
    }

    @Test
    void translatePropertiesConvertsCheckIntervalFromMillisToSeconds() {
        System.setProperty("forage.jms.pool.expiry.timeout.millis", "15000");
        ConnectionFactoryConfig config = new ConnectionFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        assertThat(props).containsEntry("quarkus.pooled-jms.connection-check-interval", "15");
    }
}
