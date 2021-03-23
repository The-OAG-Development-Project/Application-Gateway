package org.owasp.oag.filters;

import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.session.Session;

import java.util.Optional;

public class GatewayRouteContext {

    private final String routeName;
    private final GatewayRoute route;
    private final SecurityProfile securityProfile;
    private final String requestUri;
    private final String upstreamUri;
    private final Optional<Session> sessionOptional;

    public GatewayRouteContext(String routeName, GatewayRoute route, SecurityProfile securityProfile, String requestUri, String upstreamUri, Optional<Session> sessionOptional) {
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
