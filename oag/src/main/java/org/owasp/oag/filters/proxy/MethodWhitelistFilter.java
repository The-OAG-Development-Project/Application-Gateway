package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * Filter that ensures only whitelisted HTTP methods are allowed through the gateway.
 * This filter checks if the incoming request's HTTP method is in the list of allowed methods
 * defined in the route's security profile. If not, responds with 405 Method Not Allowed.
 */
@Order(10)
@Component
public class MethodWhitelistFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(MethodWhitelistFilter.class);

    /**
     * Filters incoming requests based on their HTTP method.
     *
     * @param exchange The current server exchange
     * @param chain The filter chain to continue processing
     * @param routeContext The current route context containing security profile
     * @return {@code Mono<Void>} completing when the filter chain is done
     */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute MethodWhitelistFilter");

        var reqMethod = exchange.getRequest().getMethod();
        var allowedMethods = routeContext.getSecurityProfile().getAllowedMethods();

        boolean isAllowed = allowedMethods.contains(reqMethod.name());

        if (!isAllowed) {

            logInfo(log, exchange, "Request to {} was blocked because method {} was not in list of allowed methods {}",
                    routeContext.getRequestUri(), reqMethod, allowedMethods);

            var response = exchange.getResponse();

            response.setRawStatusCode(405);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
