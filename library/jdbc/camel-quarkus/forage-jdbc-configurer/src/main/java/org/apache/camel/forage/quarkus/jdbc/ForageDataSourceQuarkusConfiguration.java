package org.apache.camel.forage.quarkus.jdbc;

import io.agroal.api.AgroalDataSource;
import io.agroal.springframework.boot.AgroalDataSourceAutoConfiguration;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.camel.forage.core.common.ServiceLoaderHelper;
import org.apache.camel.forage.core.jdbc.DataSourceProvider;
import org.apache.camel.forage.core.util.config.ConfigStore;
import org.apache.camel.forage.jdbc.factory.DataSourceFactoryConfig;
import org.apache.camel.forage.jdbc.factory.MultiDataSourceConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@AutoConfigureBefore({DataSourceAutoConfiguration.class, AgroalDataSourceAutoConfiguration.class})
public class ForageDataSourceQuarkusConfiguration implements ConfigSource {

//    @Inject
//    private

    private static final Map<String, String> configuration = new HashMap<>();

    static {
        configuration.put("my.prop", "1234");
    }

    @Override
    public int getOrdinal() {
        return 275;
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getValue(final String propertyName) {
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return InMemoryConfigSource.class.getSimpleName();
    }
}
