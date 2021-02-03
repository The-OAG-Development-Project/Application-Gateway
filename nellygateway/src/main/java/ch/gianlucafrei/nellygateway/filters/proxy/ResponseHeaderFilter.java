package ch.gianlucafrei.nellygateway.filters.proxy;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class ResponseHeaderFilter extends RouteAwareFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        return chain.filter(exchange).doOnSuccess(o -> {

            var responseHeaders = exchange.getResponse().getHeaders();

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
        });
    }
}
