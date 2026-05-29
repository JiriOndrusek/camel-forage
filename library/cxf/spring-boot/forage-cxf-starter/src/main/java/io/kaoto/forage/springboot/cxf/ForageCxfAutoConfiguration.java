package io.kaoto.forage.springboot.cxf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import io.kaoto.forage.core.annotations.FactoryType;
import io.kaoto.forage.core.annotations.FactoryVariant;
import io.kaoto.forage.core.annotations.ForageFactory;
import io.kaoto.forage.core.common.RuntimeType;
import io.kaoto.forage.core.cxf.CxfEndpointProvider;
import io.kaoto.forage.cxf.common.CxfConfig;
import io.kaoto.forage.cxf.common.CxfModuleDescriptor;
import io.kaoto.forage.cxf.soap.ForageCxfEndpoint;
import io.kaoto.forage.springboot.common.ForageSpringBootModuleAdapter;

/**
 * Auto-configuration for Forage CXF endpoint creation using ServiceLoader discovery.
 * Automatically creates CXF endpoint beans from CXF configuration properties,
 * supporting both single and multi-instance (prefixed) configurations.
 *
 * <p>Both default (unprefixed) and named/prefixed CXF endpoints are registered
 * dynamically by {@link ForageSpringBootModuleAdapter} using the {@link CxfModuleDescriptor}.
 *
 * <p>The adapter applies servlet path customization to all endpoints via a bean customizer.
 */
@ForageFactory(
        value = "CXF (Spring Boot)",
        variant = FactoryVariant.SPRING_BOOT,
        components = {"camel-cxf"},
        description = "Auto-configured CXF SOAP endpoint for Spring Boot",
        type = FactoryType.CXF_ENDPOINT,
        autowired = true,
        configClass = CxfConfig.class)
@Configuration
public class ForageCxfAutoConfiguration {

    private static final String DEFAULT_CXF_SERVLET_PATH = "/services";

    /**
     * Registers the generic module adapter that discovers both default and prefixed
     * CXF endpoint configurations and registers them as proper bean definitions
     * using the {@link CxfModuleDescriptor}.
     *
     * <p>The adapter applies a bean customizer to set the servlet container CXF path
     * for all created endpoints, ensuring proper integration with Spring Boot's servlet container.
     */
    @Bean
    static ForageSpringBootModuleAdapter<CxfConfig, CxfEndpointProvider> forageCxfModuleAdapter(
            Environment environment) {
        String cxfPath = environment.getProperty("cxf.path", DEFAULT_CXF_SERVLET_PATH);
        return new ForageSpringBootModuleAdapter<>(new CxfModuleDescriptor(), environment).withBeanCustomizer(bean -> {
            if (bean instanceof ForageCxfEndpoint endpoint) {
                endpoint.setServletContainerCxfPath(cxfPath, RuntimeType.springBoot);
            }
            return bean;
        });
    }
}
