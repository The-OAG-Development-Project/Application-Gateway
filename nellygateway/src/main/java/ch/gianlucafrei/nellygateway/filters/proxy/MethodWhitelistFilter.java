package ch.gianlucafrei.nellygateway.filters.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MethodWhitelistFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(MethodWhitelistFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        var reqMethod = exchange.getRequest().getMethodValue();
        var allowedMethods = routeContext.getSecurityProfile().getAllowedMethods();

        boolean isAllowed = allowedMethods.contains(reqMethod);

        if (!isAllowed) {

            log.info("Request to {} was blocked because method {} was not in list of allowed methods {}",
                    routeContext.getRequestUri(), reqMethod, allowedMethods);

            var response = exchange.getResponse();

            response.setRawStatusCode(405);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }
}
