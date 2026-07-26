package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * Filter that controls access to routes based on authentication status.
 */
@Order(20)
@Component
public class AccessControlFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessControlFilter.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        logTrace(log, request.servletRequest(), "Execute AccessControlFilter");

        var onlyAuthenticated = !routeContext.getRoute().isAllowAnonymous();
        var isAnonymous = routeContext.getSessionOptional().isEmpty();

        if (onlyAuthenticated && isAnonymous) {

            logInfo(log, request.servletRequest(), "Blocked unauthenticated request {} {}",
                    request.method(), routeContext.getRequestUri());

            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return next.handle(request);
    }
}
