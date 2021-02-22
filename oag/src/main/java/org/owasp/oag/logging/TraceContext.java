package org.owasp.oag.logging;

import org.springframework.web.server.ServerWebExchange;

/**
 * Represents trace implementations (i.e. correlationID implementations) to simlify log correlation and auditing/tracing
 * what has happened.
 * TraceContexts set up unique correlationId's that are logged with each log statement and are passed to downstream systems
 * to facilitate service spanning log correlation.
 * <p>
 * implementing subclases must ensure, that they setup
 * <p>
 * Tracing is configured in the central configuration as follows:
 */
public interface TraceContext {

    /**
     *
     * Processes the ServerWebExchange and returns the potentially modified ServerWebExchange object.
     * The implementation is expected to add a log correlation id to the request context the following way.
     *
     * exchange.getAttributes().put(TraceContextFilter.CONTEXT_KEY, traceId);
     *
     * @param exchange
     * @return
     */
    ServerWebExchange processExchange(ServerWebExchange exchange);
}
