package org.owasp.oag.filters.proxy;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.gateway.GatewayRouterFunctionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.util.Collection;

/**
 * Abstract base class for gateway filters that need the gateway route and its security profile.
 * Subclasses implement the three-argument filter method.
 */
public abstract class RouteAwareFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    /**
     * The main configuration of the application.
     */
    @Autowired
    MainConfig config;

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {

        // Load security profile
        String routeName = MvcUtils.getAttribute(request, GatewayRouterFunctionConfiguration.ATTRIBUTE_ROUTE_NAME);
        var gatewayRoute = config.getRoutes().get(routeName);
        var securityProfile = config.getSecurityProfiles().get(gatewayRoute.getType());

        // Load session from the request attributes
        var sessionOptional = ExtractAuthenticationFilter.extractSessionFromRequest(request.servletRequest());

        // Load additional request info
        Collection<URI> uris = MvcUtils.getAttribute(request, MvcUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        var requestUri = (uris == null || uris.isEmpty()) ? "Unknown" : uris.iterator().next().toString();
        URI upstreamUri = MvcUtils.getAttribute(request, MvcUtils.GATEWAY_REQUEST_URL_ATTR);

        var routeContext = new GatewayRouteContext(routeName, gatewayRoute, securityProfile,
                requestUri, upstreamUri.toString(), sessionOptional);

        return filter(request, next, routeContext);
    }

    /**
     * Filters the request using the provided route context.
     *
     * @param request The current request
     * @param next The next handler in the chain
     * @param routeContext The context containing route information, security profile and session details
     * @return The response
     * @throws Exception if the downstream handler fails
     */
    public abstract ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                          GatewayRouteContext routeContext) throws Exception;
}
