package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Abstract filter that reads and processes the request body.
 * This filter provides the infrastructure to read HTTP request bodies and pass
 * them to concrete implementations for processing.
 */
public abstract class ReadRequestBodyFilter extends RouteAwareFilter {

    /**
     * Filters the HTTP request by optionally reading and processing the request body.
     * If the request has no body, or if the filter should not run based on the
     * implementation's shouldRun logic, the filter chain continues without body processing.
     *
     * @param exchange the current server exchange
     * @param chain the filter chain to delegate to if the filter doesn't apply
     * @param routeContext the context for the current gateway route
     * @return a Mono that completes when the filter processing is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        var contentLengthHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);

        if (shouldRun(exchange, routeContext)) {

            if (contentLengthHeader == null) {
                // We dont have an actual body
                consumeBody(exchange, null, routeContext);
                return chain.filter(exchange);
            } else {
                // Load actual body with ModifyRequestBodyGatewayFilter that cares about everything
                var factory = new ModifyRequestBodyGatewayFilterFactory();
                var config = new ModifyRequestBodyGatewayFilterFactory.Config();
                config.setContentType(null);
                config.setInClass(String.class);
                config.setOutClass(String.class);
                config.setRewriteFunction((e, s) -> rewrite(e, s, routeContext));

                return factory.apply(config).filter(exchange, chain);
            }

        } else {
            return chain.filter(exchange);
        }

    }

    /**
     * Determines whether the filter should execute for the current request.
     * 
     * @param exchange the current server exchange
     * @param routeContext the context for the current gateway route
     * @return true if the filter should run, false otherwise
     */
    protected abstract boolean shouldRun(ServerWebExchange exchange, GatewayRouteContext routeContext);

    /**
     * Processes the request body.
     * Implementations should apply their business logic to the body in this method.
     * 
     * @param exchange the current server exchange
     * @param body the body of the request, may be null if no body is present
     * @param routeContext the context for the current gateway route
     */
    protected abstract void consumeBody(ServerWebExchange exchange, String body, GatewayRouteContext routeContext);

    /**
     * Helper method to handle the rewrite function callback from ModifyRequestBodyGatewayFilterFactory.
     * Passes the exchange and body to the consumeBody method and returns the body unchanged.
     * 
     * @param e the server exchange
     * @param s the request body
     * @param routeContext the context for the current gateway route
     * @return a Mono containing the original body
     */
    private Mono<Object> rewrite(Object e, Object s, GatewayRouteContext routeContext) {

        consumeBody((ServerWebExchange) e, (String) s, routeContext);
        return Mono.justOrEmpty(s);
    }
}
