package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;
import org.owasp.oag.session.Session;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * This implementation of a user mapper adds the user information directly as X-OAG headers. For instance
 * the username is added as X-OAG-USER: foobar.
 * <p>
 * This is less secure than the default user mapping with JWT tokens but can be used for testing purposes or
 * if the downstream system can only extract the user principal from the header. In that case one must make sure that
 * OAg cannot be bypassed because an attacker could easily craft this header by theirs own.
 */
public class RequestHeaderUserMapper implements UserMapper {

    /**
     * Header name for identifying the authentication provider
     */
    public static final String X_OAG_PROVIDER = "X-OAG-Provider";

    /**
     * Prefix for headers containing user information
     */
    public static final String X_OAG_USER_PREFIX = "X-OAG-USER-";

    private final RequestHeaderUserMappingSettings settings;


    /**
     * Constructs a RequestHeaderUserMapper with the specified settings
     *
     * @param settings The settings for header-based user mapping
     */
    public RequestHeaderUserMapper(RequestHeaderUserMappingSettings settings) {
        this.settings = settings;
        settings.requireValidSettings();
    }

    @Override
    public ServerRequest mapUserToRequest(ServerRequest request, GatewayRouteContext context) {

        var sessionOptional = context.getSessionOptional();
        if (sessionOptional == null || sessionOptional.isEmpty())
            return request;

        var builder = ServerRequest.from(request);
        Session session = sessionOptional.get();
        var engine = new UserMappingTemplatingEngine(session);

        // set() rather than header(), because header() appends and an inbound header of the same name
        // must not survive next to the one OAG produces.
        for (var entry : settings.mappings.entrySet()) {
            builder.headers(headers -> headers.set(entry.getKey(), engine.processTemplate(entry.getValue())));
        }

        return builder.build();
    }
}
