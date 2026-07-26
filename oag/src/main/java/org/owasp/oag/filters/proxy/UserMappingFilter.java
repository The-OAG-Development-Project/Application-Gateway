package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.factories.UserMappingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

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

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        logTrace(log, request.servletRequest(), "Execute DownstreamAuthenticationFilter");

        var securityProfileName = routeContext.getRoute().getType();
        var userMapper = mappingFactory.getUserMapperForSecurityProfile(securityProfileName);

        return next.handle(userMapper.mapUserToRequest(request, routeContext));
    }
}
