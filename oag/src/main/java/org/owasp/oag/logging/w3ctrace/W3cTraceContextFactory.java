package org.owasp.oag.logging.w3ctrace;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.logging.TraceContext;
import org.owasp.oag.logging.TraceContextFactory;
import org.springframework.stereotype.Component;

/**
 * Creates {@link W3cTraceContext} instances.
 */
@Component
public class W3cTraceContextFactory implements TraceContextFactory {

    /**
     * The {@code traceProfile.type} value handled by this factory.
     */
    public static final String TYPE = "w3cTraceContext";

    private final MainConfig config;

    /**
     * Creates the factory.
     *
     * @param config The main configuration, passed to every created context
     */
    public W3cTraceContextFactory(MainConfig config) {
        this.config = config;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public TraceContext create() {
        return new W3cTraceContext(config);
    }
}
