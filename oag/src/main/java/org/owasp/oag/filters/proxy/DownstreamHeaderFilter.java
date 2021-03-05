package org.owasp.oag.filters.proxy;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * This gateway filter adds additional headers to the request
 */
@Order(41)
@Component
public class DownstreamHeaderFilter extends RouteAwareFilter {

    public static final String X_PROXY = "X-PROXY";
    public static final String X_OAG_STATUS = "X-OAG-Status";
    public static final String X_PROXY_VALUE = "OWASP Application Gateway";
    private static final Logger log = LoggerFactory.getLogger(DownstreamHeaderFilter.class);
    @Autowired
    MainConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute ResponseHeaderFilter");

        var request = exchange.getRequest().mutate();
        request = request.header(X_PROXY, X_PROXY_VALUE);

        if (routeContext.getSessionOptional().isPresent()){
            request = request.header(X_OAG_STATUS, SessionInformation.SESSION_STATE_AUTHENTICATED);
        }
        else {
            request = request.header(X_OAG_STATUS, SessionInformation.SESSION_STATE_ANONYMOUS);
        }

        return chain.filter(exchange.mutate().request(request.build()).build());
    }
}
