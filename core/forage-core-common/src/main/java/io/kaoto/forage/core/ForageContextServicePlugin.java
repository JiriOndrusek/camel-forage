package io.kaoto.forage.core;

import io.kaoto.forage.core.common.BeanFactory;
import io.kaoto.forage.core.util.config.ConfigHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.ContextServicePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForageContextServicePlugin implements ContextServicePlugin {
    private static final Logger LOG = LoggerFactory.getLogger(ForageContextServicePlugin.class);

    @Override
    public void load(CamelContext camelContext) {
        ServiceLoader<BeanFactory> loader =
                ServiceLoader.load(BeanFactory.class, camelContext.getApplicationContextClassLoader());

        // get list of excluded providers
        Optional<String> excludedFactoriesString = ConfigHelper.getQuarkusProperty("forage.excluded.bean.factories");
        Set<String> excludedFactories = excludedFactoriesString.isEmpty()
                ? Collections.emptySet()
                : excludedFactoriesString.stream()
                        .flatMap(s -> Arrays.stream(s.split(",")))
                        .map(String::trim)
                        .filter(str -> !str.isEmpty())
                        .collect(Collectors.toSet());

        loader.forEach(beanFactory -> {
            if (excludedFactories.contains(beanFactory.getClass().getName())) {
                LOG.debug(
                        "Skipping configured bean factory: {}",
                        beanFactory.getClass().getName());
                return;
            }
            try {
                beanFactory.setCamelContext(camelContext);
                beanFactory.configure();
                LOG.debug(
                        "Successfully configured bean factory: {}",
                        beanFactory.getClass().getName());
            } catch (Exception e) {
                LOG.warn(
                        "Failed to configure bean factory: {}",
                        beanFactory.getClass().getName(),
                        e);
            }
        });
    }
}
