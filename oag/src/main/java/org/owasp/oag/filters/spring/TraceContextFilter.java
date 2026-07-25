package org.owasp.oag.filters.spring;

import org.jspecify.annotations.NonNull;
import org.owasp.oag.logging.TraceContextBridge;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *  Responsible to setup a trace id based on the configured Trace mechanism.
 *
 *  The trace id is made available in the reactive context and to the log implementation when using the LoggingUtils.
 */
@Order(-10)
@Component
public class TraceContextFilter implements WebFilter {

    /**
     * Logger for this class.
     */
    final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);

    /**
     * The key where the trace ID of the current request is stored.
     * This ID is stored both in the Reactive context and in the MDC (Mapped Diagnostic Context)
     * that is used for logging.
     */
    public static final String TRACE_ID_CONTEXT_KEY = "oag.CorrId";

    /**
     * The key under which the {@link org.owasp.oag.logging.TraceHeaders} of the current request is stored
     * in the exchange attributes.
     */
    public static final String TRACE_HEADERS_ATTRIBUTE = "oag.TraceHeaders";

    /**
     * Bridge for trace context operations.
     * Used to process the exchange and extract or generate trace IDs.
     */
    @Autowired
    TraceContextBridge traceContext;

    /**
     * Filters the web request by setting up a trace ID in the reactive context.
     * This method processes the exchange to extract or generate a trace ID,
     * adds it to the reactive context, and passes it along the filter chain.
     *
     * @param exchange The server web exchange containing the request and response
     * @param webFilterChain The web filter chain for executing the next filter
     * @return A Mono that completes when the filter has been applied
     */
    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain webFilterChain) {

        var requestHeaders = exchange.getRequest().getHeaders();
        var mainHeaderName = traceContext.getMainRequestHeader();
        var secondaryHeaderName = traceContext.getSecondaryRequestHeader();
        var traceHeaders = traceContext.createTraceHeaders(
                mainHeaderName == null ? null : requestHeaders.getFirst(mainHeaderName),
                secondaryHeaderName == null ? null : requestHeaders.getFirst(secondaryHeaderName));

        exchange.getAttributes().put(TRACE_ID_CONTEXT_KEY, traceHeaders.traceId());
        exchange.getAttributes().put(TRACE_HEADERS_ATTRIBUTE, traceHeaders);
        traceHeaders.responseHeaders().forEach((name, value) -> exchange.getResponse().getHeaders().add(name, value));

        LoggingUtils.logDebug(log, exchange, "Tracing started.");

        return Mono.just(traceHeaders.traceId())
                .doOnEach(LoggingUtils.logOnNext(s -> log.debug("Generated new request id.")))
                .then(webFilterChain.filter(exchange))
                .contextWrite(c -> c.put(TRACE_ID_CONTEXT_KEY, traceHeaders.traceId()));
    }
}
