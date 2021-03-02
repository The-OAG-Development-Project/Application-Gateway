package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.filters.proxy.RouteAwareFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface TokenMapper {

    Mono<ServerWebExchange> mapToken(ServerWebExchange exchange, RouteAwareFilter.GatewayRouteContext context);
}
