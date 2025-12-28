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

/**
 * Filter that applies user mapping to downstream requests.
 * This filter maps user information from the authenticated session to the 
 * appropriate format expected by downstream services, such as adding specific 
 * headers or tokens based on the security profile configuration.
 */
@Component
@Order(42)
public class UserMappingFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(UserMappingFilter.class);

    @Autowired
    private UserMappingFactory mappingFactory;

    /**
     * Applies user mapping to the request based on the security profile.
     * Gets the appropriate user mapper for the security profile and uses it to 
     * add user information to the request before forwarding it downstream.
     *
     * @param exchange The current server web exchange
     * @param chain The gateway filter chain
     * @param routeContext The context containing route information
     * @return A Mono&gt;Void&lt; that completes when the filter processing is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute DownstreamAuthenticationFilter");

        var securityProfileName = routeContext.getRoute().getType();
        var userMapper = mappingFactory.getUserMapperForSecurityProfile(securityProfileName);

        var potentiallyMutatedExchange = userMapper.mapUserToRequest(exchange, routeContext);

        return potentiallyMutatedExchange.flatMap(exg -> chain.filter(exg));
    }
}
