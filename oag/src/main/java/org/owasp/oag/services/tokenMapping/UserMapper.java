package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.filters.GatewayRouteContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * This is an interface for the such called user mapper. 
 */
public interface UserMapper {

    Mono<ServerWebExchange> mapToken(ServerWebExchange exchange, GatewayRouteContext context);
}
