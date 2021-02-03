package ch.gianlucafrei.nellygateway.filters.proxy;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public abstract class ReadRequestBodyFilter extends RouteAwareFilter {

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

    private Mono<Object> rewrite(Object e, Object s, GatewayRouteContext routeContext) {

        consumeBody((ServerWebExchange) e, (String) s, routeContext);
        return Mono.just(s);
    }

    protected abstract boolean shouldRun(ServerWebExchange exchange, GatewayRouteContext routeContext);

    protected abstract void consumeBody(ServerWebExchange exchange, String body, GatewayRouteContext routeContext);
}
