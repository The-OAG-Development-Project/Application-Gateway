package org.owasp.oag.filters.spring;

import org.jetbrains.annotations.NotNull;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Order(20)
@Component
public class SimpleLogFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {

        LoggingUtils.logTrace(log, exchange, "Execute SimpleLogFilter");

        var request = exchange.getRequest();
        LoggingUtils.logDebug(log, exchange, "Request to {} {}",
                request.getMethod(),
                request.getURI());

        return chain.filter(exchange)
                .doOnSuccess((u) -> {
                    var response = exchange.getResponse();
                    LoggingUtils.logInfo(log, exchange, "Response status code {} for {} {}",
                            response.getStatusCode(),
                            request.getMethod(),
                            request.getURI());
                })
                .doOnError(ResponseStatusException.class, e -> {

                    LoggingUtils.logInfo(log, exchange, "Response status code {} for {} {} errorReason: '{}'",
                            e.getStatusCode(),
                            request.getMethod(),
                            request.getURI(),
                            e.getReason());
                    throw e;
                });
    }
}
