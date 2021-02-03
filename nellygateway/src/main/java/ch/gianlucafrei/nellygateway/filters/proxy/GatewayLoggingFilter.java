package ch.gianlucafrei.nellygateway.filters.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayLoggingFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {


        log.debug("Incoming request {} processed by route {} is forwarded to {}",
                routeContext.getRequestUri(), routeContext.getRouteName(), routeContext.getUpstreamUri());

        return chain.filter(exchange);
    }
}
