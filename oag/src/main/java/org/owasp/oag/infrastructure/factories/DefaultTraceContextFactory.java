package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.TraceProfile;
import org.owasp.oag.logging.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultTraceContextFactory implements TraceContextFactory {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MainConfig config;

    @Override
    public TraceContext createContextForRequest(){

        TraceProfile traceProfile = config.getTraceProfile();
        TraceContext implClass = context.getBean(traceProfile.getType(), TraceContext.class);

        if (implClass == null) {
            throw new RuntimeException("Trace implementation class not found: " + traceProfile.getType());
        }
        return implClass;
    }
}
