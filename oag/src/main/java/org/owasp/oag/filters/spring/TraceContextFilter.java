package org.owasp.oag.filters.spring;

import org.owasp.oag.logging.TraceContext;
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

@Order(-10)
@Component
/**
 *  Generates a new log correlation id and adds it the response.
 *  Also adds it to the subscriber context and has
 */
public class TraceContextFilter implements WebFilter {

    final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);

    public static final String CONTEXT_KEY = "oag.CorrId";

    @Autowired
    TraceContext traceContext;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain webFilterChain) {

        // Get trace id
        exchange = traceContext.processExchange(exchange);

        String possibleTraceId = exchange.getAttribute(CONTEXT_KEY);
        final String traceId = possibleTraceId == null ? "n/a" : possibleTraceId;

        // Add request id to subscription context and make a log statement
        return Mono.just(traceId)
                .doOnEach(LoggingUtils.logOnNext(s -> {
                    log.info("Generated new request id: {}", s);
                }))
                .then(webFilterChain.filter(exchange))
                .contextWrite(c -> c.put(CONTEXT_KEY, traceId));
    }
}