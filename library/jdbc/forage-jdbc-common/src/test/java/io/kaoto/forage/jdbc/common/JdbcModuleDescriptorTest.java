package io.kaoto.forage.jdbc.common;

import java.util.Map;
import io.kaoto.forage.core.util.config.ConfigStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JdbcModuleDescriptorTest {

    private final JdbcModuleDescriptor descriptor = new JdbcModuleDescriptor();

    @BeforeEach
    void setUp() {
        System.setProperty("forage.jdbc.db.kind", "postgresql");
        System.setProperty("forage.jdbc.url", "jdbc:postgresql://localhost:5432/test");
        System.setProperty("forage.jdbc.username", "testuser");
        System.setProperty("forage.jdbc.password", "testpass");
        System.setProperty("forage.jdbc.pool.min.size", "3");
        System.setProperty("forage.jdbc.pool.max.size", "15");
        ConfigStore.getInstance().reload();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("forage.jdbc.db.kind");
        System.clearProperty("forage.jdbc.url");
        System.clearProperty("forage.jdbc.username");
        System.clearProperty("forage.jdbc.password");
        System.clearProperty("forage.jdbc.pool.min.size");
        System.clearProperty("forage.jdbc.pool.max.size");
        System.clearProperty("forage.jdbc.transaction.enabled");
        System.clearProperty("forage.jdbc.transaction.node.id");
        System.clearProperty("forage.jdbc.transaction.object.store.datasource");
        ConfigStore.getInstance().reload();
    }

    @Test
    void translatePropertiesSetsMaxSizeFromMaxSize() {
        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        String prefix = "quarkus.datasource.\"dataSource\".";
        assertThat(props).containsEntry(prefix + "jdbc.min-size", "3");
        assertThat(props).containsEntry(prefix + "jdbc.max-size", "15");
    }

    @Test
    void translatePropertiesOmitsNullTransactionStrings() {
        System.setProperty("forage.jdbc.transaction.enabled", "true");
        ConfigStore.getInstance().reload();

        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        assertThat(props).doesNotContainKey("quarkus.transaction-manager.node-name");
        assertThat(props).doesNotContainKey("quarkus.transaction-manager.object-store.datasource");
        assertThat(props.values()).noneMatch("null"::equals);
    }

    @Test
    void translatePropertiesIncludesTransactionStringsWhenSet() {
        System.setProperty("forage.jdbc.transaction.enabled", "true");
        System.setProperty("forage.jdbc.transaction.node.id", "node-1");
        System.setProperty("forage.jdbc.transaction.object.store.datasource", "myDS");
        ConfigStore.getInstance().reload();

        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        assertThat(props).containsEntry("quarkus.transaction-manager.node-name", "node-1");
        assertThat(props).containsEntry("quarkus.transaction-manager.object-store.datasource", "myDS");
    }

    @Test
    void translatePropertiesUsesDistinctMinAndMaxDefaults() {
        System.clearProperty("forage.jdbc.pool.min.size");
        System.clearProperty("forage.jdbc.pool.max.size");
        ConfigStore.getInstance().reload();

        System.setProperty("forage.jdbc.db.kind", "postgresql");
        System.setProperty("forage.jdbc.url", "jdbc:postgresql://localhost:5432/test");
        System.setProperty("forage.jdbc.username", "testuser");
        System.setProperty("forage.jdbc.password", "testpass");

        DataSourceFactoryConfig config = new DataSourceFactoryConfig();
        Map<String, String> props = descriptor.translateProperties(null, config);

        String prefix = "quarkus.datasource.\"dataSource\".";
        assertThat(props).containsEntry(prefix + "jdbc.min-size", "2");
        assertThat(props).containsEntry(prefix + "jdbc.max-size", "20");
    }
}
