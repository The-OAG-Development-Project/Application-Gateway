package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.factories.UserMappingFactory;
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
public class UserMappingFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(UserMappingFilter.class);

    @Autowired
    private UserMappingFactory mappingFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute DownstreamAuthenticationFilter");

        var securityProfileName = routeContext.getRoute().getType();
        var userMapper = mappingFactory.getUserMapperForSecurityProfile(securityProfileName);

        var potentiallyMutatedExchange = userMapper.mapUserToRequest(exchange, routeContext);

        return potentiallyMutatedExchange.flatMap(exg -> chain.filter(exg));
    }
}
