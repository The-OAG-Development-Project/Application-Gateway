package org.owasp.oag.filters.proxy;

import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.gateway.SpringCloudGatewayConfiguration;
import org.owasp.oag.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * Abstract base class for gateway filter that also loads the gateway route and security profile
 */
public abstract class RouteAwareFilter implements GlobalFilter {

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

    public abstract Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext);

    public class GatewayRouteContext {

        private final String routeName;
        private final GatewayRoute route;
        private final SecurityProfile securityProfile;
        private final String requestUri;
        private final String upstreamUri;
        private final Optional<Session> sessionOptional;

        private GatewayRouteContext(String routeName, GatewayRoute route, SecurityProfile securityProfile, String requestUri, String upstreamUri, Optional<Session> sessionOptional) {
            this.routeName = routeName;
            this.route = route;
            this.securityProfile = securityProfile;
            this.requestUri = requestUri;
            this.upstreamUri = upstreamUri;
            this.sessionOptional = sessionOptional;
        }

        public String getRouteName() {
            return routeName;
        }

        public GatewayRoute getRoute() {
            return route;
        }

        public SecurityProfile getSecurityProfile() {
            return securityProfile;
        }

        public String getRequestUri() {
            return requestUri;
        }

        public String getUpstreamUri() {
            return upstreamUri;
        }

        public Optional<Session> getSessionOptional() {
            return sessionOptional;
        }
    }
}
