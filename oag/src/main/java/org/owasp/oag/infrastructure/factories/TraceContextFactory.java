package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.logging.TraceContext;

/**
 * Factory interface to create TraceContext. Note that while most of the other factories return singletons,
 * this factories produce each time a new instance!
 */
public interface TraceContextFactory {

    /**
     * @return a new instance of the configured trace context.
     */
    TraceContext createContextForRequest();
}
