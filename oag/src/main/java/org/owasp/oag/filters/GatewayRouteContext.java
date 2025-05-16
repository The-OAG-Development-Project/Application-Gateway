package org.owasp.oag.filters;

import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.session.Session;

import java.util.Optional;

/**
 * Contains context information about the current gateway route processing.
 * This class aggregates all information needed during request routing and filtering,
 * including route configuration, security profiles, and session information.
 */
public class GatewayRouteContext {

    private final String routeName;
    private final GatewayRoute route;
    private final SecurityProfile securityProfile;
    private final String requestUri;
    private final String upstreamUri;
    private final Optional<Session> sessionOptional;

    /**
     * Creates a new gateway route context with all required information.
     *
     * @param routeName The name of the route being processed
     * @param route The gateway route configuration
     * @param securityProfile The security profile applied to the route
     * @param requestUri The original request URI
     * @param upstreamUri The URI to which the request will be forwarded
     * @param sessionOptional The optional user session information
     */
    public GatewayRouteContext(String routeName, GatewayRoute route, SecurityProfile securityProfile, String requestUri, String upstreamUri, Optional<Session> sessionOptional) {
        this.routeName = routeName;
        this.route = route;
        this.securityProfile = securityProfile;
        this.requestUri = requestUri;
        this.upstreamUri = upstreamUri;
        this.sessionOptional = sessionOptional;
    }

    /**
     * Gets the name of the route being processed.
     *
     * @return The route name
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * Gets the gateway route configuration.
     *
     * @return The gateway route configuration
     */
    public GatewayRoute getRoute() {
        return route;
    }

    /**
     * Gets the security profile applied to the route.
     *
     * @return The security profile
     */
    public SecurityProfile getSecurityProfile() {
        return securityProfile;
    }

    /**
     * Gets the original request URI.
     *
     * @return The request URI
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * Gets the URI to which the request will be forwarded.
     *
     * @return The upstream URI
     */
    public String getUpstreamUri() {
        return upstreamUri;
    }

    /**
     * Gets the optional user session information.
     *
     * @return An Optional containing the session if present
     */
    public Optional<Session> getSessionOptional() {
        return sessionOptional;
    }
}
