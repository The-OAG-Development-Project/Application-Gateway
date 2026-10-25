package org.owasp.oag.logging;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link TraceContextFactory} implementations, keyed by {@code traceProfile.type}.
 */
@Component
public class TraceContextRegistry extends TypeRegistry<TraceContextFactory> {

    /**
     * Creates the registry.
     *
     * @param factories All trace context factories
     */
    public TraceContextRegistry(List<TraceContextFactory> factories) {
        super("trace context", factories);
    }
}
