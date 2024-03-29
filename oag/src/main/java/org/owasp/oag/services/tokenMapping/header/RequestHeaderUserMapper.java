package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;
import org.owasp.oag.session.Session;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * This implementation of a user mapper adds the user information directly as X-OAG headers. For instance
 * the username is added as X-OAG-USER: foobar.
 * <p>
 * This is less secure than the default user mapping with JWT tokens but can be used for testing purposes or
 * if the downstream system can only extract the user principal from the header. In that case one must make sure that
 * OAg cannot be bypassed because an attacker could easily craft this header by theirs own.
 */
public class RequestHeaderUserMapper implements UserMapper {

    public static final String X_OAG_PROVIDER = "X-OAG-Provider";
    public static final String X_OAG_USER_PREFIX = "X-OAG-USER-";

    private final RequestHeaderUserMappingSettings settings;


    public RequestHeaderUserMapper(RequestHeaderUserMappingSettings settings) {
        this.settings = settings;
        settings.requireValidSettings();
    }

    @Override
    public Mono<ServerWebExchange> mapUserToRequest(ServerWebExchange exchange, GatewayRouteContext context) {

        var sessionOptional = context.getSessionOptional();
        if (sessionOptional != null && sessionOptional.isPresent()) {

            var request = exchange.getRequest().mutate();
            Session session = sessionOptional.get();
            HashMap<String, String> userMappings = session.getUserModel().getMappings();

            var engine = new UserMappingTemplatingEngine(session);
            for (var entry : settings.mappings.entrySet()) {

                var value = engine.processTemplate(entry.getValue());
                request.header(entry.getKey(), value);
            }

            exchange = exchange.mutate().request(request.build()).build();

        }

        return Mono.just(exchange);
    }
}
