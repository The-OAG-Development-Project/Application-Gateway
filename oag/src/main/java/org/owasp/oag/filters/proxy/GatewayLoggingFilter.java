package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.owasp.oag.utils.LoggingUtils.logDebug;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * Gateway filter responsible for logging request routing information.
 * This filter logs relevant details about incoming requests and how they are being routed,
 * providing visibility into the routing process for debugging and monitoring purposes.
 */
@Order(100)
@Component
public class GatewayLoggingFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoggingFilter.class);

    /**
     * Filters the incoming request by logging routing information.
     * Logs the incoming request URI, the selected route name, and the upstream URI 
     * to which the request is being forwarded.
     *
     * @param exchange The current server web exchange
     * @param chain The gateway filter chain
     * @param routeContext The context containing route information
     * @return A {@code Mono<Void>} that completes when the filter processing is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute GatewayLoggingFilter");
        logDebug(log, exchange, "Incoming request {} processed by route {} is forwarded to {}",
                routeContext.getRequestUri(), routeContext.getRouteName(), routeContext.getUpstreamUri());

        return chain.filter(exchange);
    }
}
