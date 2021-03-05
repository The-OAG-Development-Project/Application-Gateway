package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.filters.GatewayRouteContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * This is an interface for the such called user mapper. A user mapper is responsible for adding the user information
 * to the downstream request.
 *
 * The default method of user mapping is to add a signed jwt token to the request that contains
 * the user information. However, if you want another method of transporting the user information e.g. in a custom header
 * or so you can create a new bean of this type and load the implementation via the configuration file.
 *
 * Implementation must be non-blocking. If you run blocking code please use the util functions from the ReactiveUtils class
 */
public interface UserMapper {

    /**
     * Add the information about the user to the request. Must return a potentially mutated ServerWebExchange object.
     * @param exchange The current server web exchange object
     * @param context  Additional information about the request such as user session, urls and so on.
     * @return Mutated server web exhange
     */
    Mono<ServerWebExchange> mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context);
}
