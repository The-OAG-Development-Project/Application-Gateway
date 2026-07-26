package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.ArrayList;
import java.util.Collections;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * Filter that modifies response headers according to the security profile.
 * Can add, modify, or remove headers from the response.
 */
@Order(40)
@Component
public class ResponseHeaderFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(ResponseHeaderFilter.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        var response = next.handle(request);

        logTrace(log, request.servletRequest(), "Execute ResponseHeaderFilter");

        var responseHeaders = response.headers();

        // Change headers according to security policy
        for (var entry : routeContext.getSecurityProfile().getResponseHeaders().entrySet()) {

            String name = entry.getKey();
            String value = entry.getValue();

            if ("<<remove>>".equals(value)) {
                responseHeaders.remove(name);
            } else {
                responseHeaders.put(name, new ArrayList<>(Collections.singleton(value)));
            }
        }

        return response;
    }
}
