package org.owasp.oag.logging.simpleTrace;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.logging.TraceContext;
import org.owasp.oag.logging.TraceContextFactory;
import org.springframework.stereotype.Component;

/**
 * Creates {@link SimpleTraceContext} instances.
 */
@Component
public class SimpleTraceContextFactory implements TraceContextFactory {

    /**
     * The {@code traceProfile.type} value handled by this factory.
     */
    public static final String TYPE = "simpleTraceContext";

    private final MainConfig config;

    /**
     * Creates the factory.
     *
     * @param config The main configuration, passed to every created context
     */
    public SimpleTraceContextFactory(MainConfig config) {
        this.config = config;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public TraceContext create() {
        return new SimpleTraceContext(config);
    }
}
