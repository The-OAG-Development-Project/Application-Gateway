package org.owasp.oag.filters.spring;

import org.jetbrains.annotations.NotNull;
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

    final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);

    // This is the key where the trace id of the current request is stored, both in the Reactive context as well as in
    // in the MDC that is used for logging.
    public static final String TRACE_ID_CONTEXT_KEY = "oag.CorrId";

    @Autowired
    TraceContextBridge traceContext;

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain webFilterChain) {

        // Get trace id
        exchange = traceContext.processExchange(exchange);

        String possibleTraceId = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
        final String traceId = possibleTraceId == null ? "noTraceIdProvided" : possibleTraceId;

        // Add request id to subscription context and make a log statement
        return Mono.just(traceId)
                .doOnEach(LoggingUtils.logOnNext(s -> log.debug("Generated new request id.")))
                .then(webFilterChain.filter(exchange))
                .contextWrite(c -> c.put(TRACE_ID_CONTEXT_KEY, traceId));
    }
}