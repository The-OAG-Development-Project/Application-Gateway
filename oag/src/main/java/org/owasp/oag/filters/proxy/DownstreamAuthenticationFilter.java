package org.owasp.oag.filters.proxy;

import org.owasp.oag.services.tokenMapping.TokenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

@Component
@Order(42)
public class DownstreamAuthenticationFilter extends RouteAwareFilter{

    private static final Logger log = LoggerFactory.getLogger(DownstreamAuthenticationFilter.class);

    @Autowired
    private TokenMapper tokenMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute DownstreamAuthenticationFilter");

        var potentiallyMutatedExchange = tokenMapper.mapToken(exchange, routeContext);

        return potentiallyMutatedExchange.flatMap(exg -> chain.filter(exg));
    }
}
