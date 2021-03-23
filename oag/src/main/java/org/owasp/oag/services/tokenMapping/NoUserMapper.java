package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.filters.GatewayRouteContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * This implementation of a user mapper does not pass any information to the downstream system.
 * Can be used if user mapping should be disabled from some reason
 */
public class NoUserMapper implements UserMapper {

    @Override
    public Mono<ServerWebExchange> mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context) {
        return Mono.just(exchange);
    }
}
