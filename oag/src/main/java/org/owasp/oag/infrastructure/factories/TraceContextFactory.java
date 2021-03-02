package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.logging.TraceContext;

public interface TraceContextFactory {

    TraceContext createContextForRequest();
}
