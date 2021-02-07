package ch.gianlucafrei.nellygateway.filters.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class SimpleLogFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        beforeRequest(exchange);

        return chain.filter(exchange)
                .doOnSuccess((d) -> afterRequestProcessed(exchange));
    }

    public void beforeRequest(ServerWebExchange exchange) {

        var request = exchange.getRequest();

        log.info("Request to {} {}",
                request.getMethod(),
                request.getURI());
    }

    protected void afterRequestProcessed(ServerWebExchange exchange) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        log.info("Response status code {} for {} {}",
                response.getRawStatusCode(),
                request.getMethodValue(),
                request.getURI());
    }
}
