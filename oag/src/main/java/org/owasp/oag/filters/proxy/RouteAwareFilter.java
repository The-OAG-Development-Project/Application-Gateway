package org.owasp.oag.filters.proxy;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.gateway.SpringCloudGatewayConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * Abstract base class for gateway filters that load the gateway route and security profile.
 * This class implements the GlobalFilter interface and provides common functionality
 * for filters that need access to route information and security profiles.
 * Subclasses must implement the abstract filter method to provide specific filtering behavior.
 */
public abstract class RouteAwareFilter implements GlobalFilter {

    /**
     * The main configuration of the application.
     * Contains information about routes, security profiles, and other configuration settings.
     */
    @Autowired
    MainConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // Load security profile
        var routeName = (String) exchange.getAttribute(SpringCloudGatewayConfiguration.ATTRIBUTE_ROUTE_NAME);
        var gatewayRoute = config.getRoutes().get(routeName);
        var type = gatewayRoute.getType();
        var securityProfile = config.getSecurityProfiles().get(type);

        // Load session from exchange context
        var sessionOptional = ExtractAuthenticationFilter.extractSessionFromExchange(exchange);

        // Load additional request info
        var uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
        var requestUri = (uris.isEmpty()) ? "Unknown" : uris.iterator().next().toString();
        var upstreamUri = (URI) exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);

        var routeContext = new GatewayRouteContext(routeName, gatewayRoute, securityProfile, requestUri, upstreamUri.toString(), sessionOptional);
        return filter(exchange, chain, routeContext);
    }

    /**
     * Filters the web request using the provided route context.
     * This method must be implemented by subclasses to provide specific filtering behavior.
     *
     * @param exchange The server web exchange containing the request and response
     * @param chain The gateway filter chain for executing the next filter
     * @param routeContext The context containing route information, security profile, and session details
     * @return A Mono that completes when the filter has been applied
     */
    public abstract Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext);
}
