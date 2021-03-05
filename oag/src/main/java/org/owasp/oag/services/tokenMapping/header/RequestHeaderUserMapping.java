package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * This implementation of a user mapper adds the user information directly as X-OAG headers. For instance
 * the username is added as X-OAG-USER: foobar.
 *
 * This is less secure than the default user mapping with JWT tokens but can be used for testing purposes or
 * if the downstream system can only extract the user principal from the header. In that case one must make sure that
 * OAg cannot be bypassed because an attacker could easily craft this header by theirs own.
 */
@Component("header-mapping")
public class RequestHeaderUserMapping implements UserMapper {

    public static final String X_OAG_PROVIDER = "X-OAG-Provider";
    public static final String X_OAG_USER_PREFIX = "X-OAG-USER-";

    @Override
    public Mono<ServerWebExchange> mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context) {

        var sessionOptional = context.getSessionOptional();
        if (sessionOptional != null && sessionOptional.isPresent()) {

            var request = exchange.getRequest().mutate();
            Session session = sessionOptional.get();
            HashMap<String, String> userMappings = session.getUserModel().getMappings();

            request = request.header(X_OAG_PROVIDER, session.getProvider());
            request = request.header(X_OAG_USER_PREFIX + "ID", session.getUserModel().getId());

            for (var mapping : userMappings.entrySet()) {
                request = request.header(X_OAG_USER_PREFIX + mapping.getKey(), mapping.getValue());
            }

            exchange = exchange.mutate().request(request.build()).build();

        }

        return Mono.just(exchange);
    }
}
