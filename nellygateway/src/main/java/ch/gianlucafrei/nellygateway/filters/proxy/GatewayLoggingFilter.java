package ch.gianlucafrei.nellygateway.filters.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logDebug;
import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logTrace;

@Order(100)
@Component
public class GatewayLoggingFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute GatewayLoggingFilter");
        logDebug(log, exchange, "Incoming request {} processed by route {} is forwarded to {}",
                routeContext.getRequestUri(), routeContext.getRouteName(), routeContext.getUpstreamUri());

        return chain.filter(exchange);
    }
}
