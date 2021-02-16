package ch.gianlucafrei.nellygateway.filters.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logOnNext;

@Order(-10)
@Component
/**
 *  Generates a new log correlation id and adds it the response.
 *  Also adds it to the subscriber context and has
 */
public class LogCorrelationFilter implements WebFilter {

    public static final String CONTEXT_KEY = "log-context";
    public static final String REQUEST_ID_HEADER = "Request-Id";

    final Logger log = LoggerFactory.getLogger(LogCorrelationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain webFilterChain) {

        // Generate new request id
        var requestId = UUID.randomUUID().toString().substring(0, 8);

        // Add request id to serverExchange for easy access
        exchange.getAttributes().put(CONTEXT_KEY, requestId);

        // Put request id to server response
        exchange.getResponse().beforeCommit(() -> addRequestIdToResponse(exchange));

        // Add request id to subscription context and make a log statement
        return Mono.just(requestId)
                .doOnEach(logOnNext(s -> {
                    log.info("Generated new request id: {}", s);
                }))
                .then(webFilterChain.filter(exchange))
                .contextWrite(c -> c.put(CONTEXT_KEY, requestId));
    }

    public Mono<Void> addRequestIdToResponse(ServerWebExchange serverWebExchange) {

        return Mono.deferContextual(ctx -> Mono.just(ctx))
                .doOnEach(logOnNext((ctx) -> {
                    ctx.getOrEmpty(CONTEXT_KEY).ifPresent(
                            (requestId) -> {
                                serverWebExchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId.toString());
                                log.info("Added request id {} to response header", requestId);
                            });
                })).then();
    }
}