package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

@Order(20)
@Component
public class AccessControlFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessControlFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute AccessControlFilter");

        var request = exchange.getRequest();
        var onlyAuthenticated = !routeContext.getRoute().isAllowAnonymous();
        var isAnonymous = routeContext.getSessionOptional().isEmpty();

        if (onlyAuthenticated && isAnonymous) {

            logInfo(log, exchange, "Blocked unauthenticated request {} {}", request.getMethod(), routeContext.getRequestUri());
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
