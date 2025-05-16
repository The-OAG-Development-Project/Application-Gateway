package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.TraceProfile;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.logging.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the TraceContextFactory interface.
 * This factory is responsible for creating TraceContext instances based on the
 * configured trace profile in the application configuration.
 */
@Component
public class DefaultTraceContextFactory implements TraceContextFactory {

    /**
     * The Spring application context used to retrieve TraceContext beans.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * The main configuration containing the trace profile settings.
     */
    @Autowired
    private MainConfig config;

    /**
     * Creates a TraceContext instance for the current request based on the configured trace profile.
     * The implementation class is retrieved from the Spring application context using the type
     * specified in the trace profile configuration.
     *
     * @return A TraceContext instance for the current request
     * @throws ConfigurationException if the specified trace implementation class is not found
     */
    @Override
    public TraceContext createContextForRequest(){

        TraceProfile traceProfile = config.getTraceProfile();
        TraceContext implClass = context.getBean(traceProfile.getType(), TraceContext.class);

        if (implClass == null) {
            throw new ConfigurationException("Trace implementation class not found: " + traceProfile.getType(), null);
        }
        return implClass;
    }
}
