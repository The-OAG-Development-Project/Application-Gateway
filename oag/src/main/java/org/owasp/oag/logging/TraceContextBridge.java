package org.owasp.oag.logging;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.TraceProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * This class bridges to the concrete trace context implementation that was configured in the main config yaml (section traceProfile).
 */
@Component
@Primary
public class TraceContextBridge implements TraceContext {

    private final TraceContext implClass;

    @Autowired
    public TraceContextBridge(ApplicationContext context, MainConfig config) {
        implClass = loadBean(context, config);
    }

    private TraceContext loadBean(ApplicationContext context, MainConfig config) {
        TraceProfile traceProfile = config.getTraceProfile();
        TraceContext implClass = context.getBean(traceProfile.getType(), TraceContext.class);


        if (implClass == null) {
            throw new RuntimeException("Trace implementation class not found: " + traceProfile.getType());
        }
        return implClass;
    }

    @Override
    public ServerWebExchange processExchange(ServerWebExchange exchange) {
        return implClass.processExchange(exchange);
    }
}
