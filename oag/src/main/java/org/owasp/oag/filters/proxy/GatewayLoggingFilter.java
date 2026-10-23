package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Gateway filter responsible for logging request routing information.
 * This filter logs relevant details about incoming requests and how they are being routed,
 * providing visibility into the routing process for debugging and monitoring purposes.
 */
@Order(100)
@Component
public class GatewayLoggingFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoggingFilter.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        log.trace("Execute GatewayLoggingFilter.");
        log.debug("Incoming request {} processed by route {} is forwarded to {}",
                routeContext.getRequestUri(), routeContext.getRouteName(), routeContext.getUpstreamUri());

        return next.handle(request);
    }
}
