package org.owasp.oag.infrastructure;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.TraceProfile;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.logging.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures all non-trivial beans that need the main configuration for initialization
 */
@Configuration
public class PostConfigBeanConfiguration {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MainConfig config;

    @Bean
    public TraceContext traceContext() {
        TraceProfile traceProfile = config.getTraceProfile();
        TraceContext implClass = context.getBean(traceProfile.getType(), TraceContext.class);


        if (implClass == null) {
            throw new ConfigurationException("Trace implementation class not found: " + traceProfile.getType(), null);
        }
        return implClass;
    }
}
