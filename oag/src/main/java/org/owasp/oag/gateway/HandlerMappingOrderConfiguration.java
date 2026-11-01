package org.owasp.oag.gateway;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

/**
 * Ranks the gateway routes against OAG's own endpoints: annotated controllers first, then the configured
 * gateway routes, then static resources.
 */
@Configuration
public class HandlerMappingOrderConfiguration {

    /**
     * Moves the router function mapping behind the annotated controllers.
     * <p>
     * Spring MVC registers {@code RouterFunctionMapping} at order -1, ahead of the
     * {@code RequestMappingHandlerMapping} that serves OAG's own endpoints. A configured route such as
     * {@code /**} would therefore proxy {@code /auth/...} downstream instead of letting the login
     * controller handle it. Order 1 restores the intended precedence: OAG's own endpoints first, then
     * the configured gateway routes, then static resources.
     *
     * @return The post processor that reorders the mapping
     */
    @Bean
    public static BeanPostProcessor routerFunctionMappingAfterControllers() {

        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RouterFunctionMapping mapping) {
                    mapping.setOrder(1);
                }
                return bean;
            }
        };
    }
}
