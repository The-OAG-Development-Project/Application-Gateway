package ch.gianlucafrei.nellygateway.filters.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logTrace;

@Order(40)
@Component
public class ResponseHeaderFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(ResponseHeaderFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        return chain.filter(exchange).doOnSuccess(o -> {

            logTrace(log, exchange, "Execute ResponseHeaderFilter");

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
