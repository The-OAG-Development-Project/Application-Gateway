package org.owasp.oag.logging;

import org.owasp.oag.extension.TypedExtension;

/**
 * Creates the {@link TraceContext} of one trace type, a fresh instance for each request.
 * The factory is selected through {@code traceProfile.type}, see {@link #type()}.
 */
public interface TraceContextFactory extends TypedExtension {

    /**
     * Creates a new trace context for one request.
     *
     * @return A new trace context
     */
    TraceContext create();
}
