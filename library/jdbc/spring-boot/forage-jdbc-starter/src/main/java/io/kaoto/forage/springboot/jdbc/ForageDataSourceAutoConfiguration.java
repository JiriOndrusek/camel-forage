package io.kaoto.forage.springboot.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import io.agroal.springframework.boot.AgroalDataSourceAutoConfiguration;
import io.kaoto.forage.core.annotations.FactoryType;
import io.kaoto.forage.core.annotations.FactoryVariant;
import io.kaoto.forage.core.annotations.ForageFactory;
import io.kaoto.forage.core.jdbc.DataSourceProvider;
import io.kaoto.forage.jdbc.common.DataSourceFactoryConfig;
import io.kaoto.forage.jdbc.common.JdbcModuleDescriptor;
import io.kaoto.forage.springboot.common.ForageSpringBootModuleAdapter;

/**
 * Auto-configuration for Forage DataSource creation using ServiceLoader discovery.
 * Automatically creates DataSource beans from JDBC configuration properties,
 * supporting both single and multi-instance (prefixed) configurations.
 *
 * <p>Both default (unprefixed) and named/prefixed datasources are registered dynamically
 * by {@link ForageSpringBootModuleAdapter} using the {@link JdbcModuleDescriptor}.
 *
 * <p>This configuration class handles:
 * <ul>
 *   <li>Transaction management setup (when {@code forage.jdbc.transaction.enabled=true})</li>
 *   <li>The {@link ForageSpringBootModuleAdapter} bean for dynamic registration</li>
 * </ul>
 */
@ForageFactory(
        value = "DataSource (Spring Boot)",
        components = {"camel-sql", "camel-jdbc", "camel-spring-jdbc"},
        description =
                "Auto-configured JDBC DataSource for Spring Boot with transaction management and repository support",
        type = FactoryType.DATA_SOURCE,
        autowired = true,
        configClass = DataSourceFactoryConfig.class,
        variant = FactoryVariant.SPRING_BOOT)
@AutoConfiguration(
        before = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            AgroalDataSourceAutoConfiguration.class
        })
@Import(ForageJdbcBeanRegistrar.class)
public class ForageDataSourceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ForageDataSourceAutoConfiguration.class);

    /**
     * Transaction management configuration that enables Spring transaction support
     * when JDBC transactions are configured in Forage DataSource settings.
     */
    @Configuration
    @ConditionalOnProperty(value = "forage.jdbc.transaction.enabled", havingValue = "true")
    @EnableTransactionManagement
    static class ForageTransactionManagement {

        @jakarta.annotation.PostConstruct
        public void init() {
            log.info("ForageTransactionManagement configuration enabled");
        }
    }

    /**
     * Registers the generic module adapter that discovers both default and prefixed
     * DataSource configurations and registers them as proper bean definitions using the
     * {@link JdbcModuleDescriptor}.
     *
     * <p>The adapter handles provider selection based on {@code forage.jdbc.db.kind},
     * supporting multiple database types (PostgreSQL, MySQL, etc.) on the classpath.
     */
    @Bean
    static ForageSpringBootModuleAdapter<DataSourceFactoryConfig, DataSourceProvider> forageJdbcModuleAdapter(
            Environment environment) {
        return new ForageSpringBootModuleAdapter<>(new JdbcModuleDescriptor(), environment);
    }
}
