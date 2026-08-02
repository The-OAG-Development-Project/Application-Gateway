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

/**
 * Filter that ensures only whitelisted HTTP methods are allowed through the gateway.
 * This filter checks if the incoming request's HTTP method is in the list of allowed methods
 * defined in the route's security profile. If not, responds with 405 Method Not Allowed.
 */
@Order(10)
@Component
public class MethodWhitelistFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(MethodWhitelistFilter.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        log.trace("Execute MethodWhitelistFilter.");

        var reqMethod = request.method();
        var allowedMethods = routeContext.getSecurityProfile().getAllowedMethods();

        if (!allowedMethods.contains(reqMethod.name())) {

            log.info("Request to {} was blocked because method {} was not in list of allowed methods {}",
                    routeContext.getRequestUri(), reqMethod, allowedMethods);

            return ServerResponse.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        return next.handle(request);
    }
}
