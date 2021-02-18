package ch.gianlucafrei.nellygateway.filters.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logInfo;
import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logTrace;

@Order(20)
@Component
public class SimpleLogFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        logTrace(log, exchange, "Execute SimpleLogFilter");

        var request = exchange.getRequest();
        logInfo(log, exchange, "Request to {} {}",
                request.getMethod(),
                request.getURI());

        return chain.filter(exchange)
                .doOnSuccess((u) -> {
                    var response = exchange.getResponse();
                    logInfo(log, exchange, "Response status code {} for {} {}",
                       response.getRawStatusCode(),
                       request.getMethodValue(),
                       request.getURI());
                });
    }
}
