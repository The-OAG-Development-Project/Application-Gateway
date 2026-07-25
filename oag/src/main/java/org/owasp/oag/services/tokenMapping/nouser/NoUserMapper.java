package org.owasp.oag.services.tokenMapping.nouser;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.springframework.web.server.ServerWebExchange;

/**
 * This implementation of a user mapper does not pass any information to the downstream system.
 * Can be used if user mapping should be disabled from some reason
 */
public class NoUserMapper implements UserMapper {

    /**
     * Maps user information to the request. This implementation returns the exchange unchanged,
     * so no user information reaches the downstream request.
     *
     * @param exchange The server web exchange containing the request and response
     * @param context The gateway route context containing route information
     * @return The unchanged server web exchange
     */
    @Override
    public ServerWebExchange mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context) {
        return exchange;
    }
}
