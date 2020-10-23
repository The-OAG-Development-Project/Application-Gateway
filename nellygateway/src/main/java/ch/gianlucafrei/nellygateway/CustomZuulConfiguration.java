package ch.gianlucafrei.nellygateway;

import com.google.common.collect.Sets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * This configuration class replaces the usual config file configuration of zuul
 */
@Configuration
public class CustomZuulConfiguration {

    @Primary
    @Bean(name = "zuul.CONFIGURATION_PROPERTIES")
    @RefreshScope
    @ConfigurationProperties("zuul")
    public ZuulProperties zuulProperties() {

        ZuulProperties zuulProperties = new ZuulProperties();

        // Ignored pattern
        zuulProperties.setIgnoredPatterns(Sets.newHashSet("/auth/**", "/error"));

        // Routes
        zuulProperties.setRoutes(NellygatewayApplication.config.getRoutesAsZuulRoutes());

        return zuulProperties;
    }

}
