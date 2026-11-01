package org.owasp.oag.logging;

import org.springframework.stereotype.Component;

/**
 * Creates {@link NoTraceContext} instances, which disable correlation logging.
 */
@Component
public class NoTraceContextFactory implements TraceContextFactory {

    /**
     * The {@code traceProfile.type} value handled by this factory.
     */
    public static final String TYPE = "noTraceContext";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public TraceContext create() {
        return new NoTraceContext();
    }
}
