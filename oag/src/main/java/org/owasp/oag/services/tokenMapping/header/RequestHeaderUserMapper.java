package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;
import org.owasp.oag.session.Session;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * This implementation of a user mapper passes the user information to the downstream system in plain
 * request headers.
 * <p>
 * It adds exactly the headers named in the security profile's user mapping {@code mappings} section, with
 * each value rendered by the {@link UserMappingTemplatingEngine}. The header names come entirely from
 * configuration - this mapper imposes no naming scheme of its own. For example:
 * <pre>
 * userMapping:
 *   type: "requestHeader"
 *   settings:
 *     mappings:
 *       X-USER-PROVIDER: &lt;session.provider&gt;
 *       X-USER-ID: &lt;session.userId&gt;
 * </pre>
 * When there is no user session, the request is forwarded unchanged.
 * <p>
 * This is less secure than the default user mapping with JWT tokens but can be used for testing purposes or
 * if the downstream system can only extract the user principal from the header. In that case one must make sure that
 * OAG cannot be bypassed, because an attacker could otherwise craft these headers themselves.
 */
public class RequestHeaderUserMapper implements UserMapper {

    /**
     * Conventional prefix for headers carrying user information, offered for use in the {@code mappings}
     * configuration. This mapper does not apply it - a mapping's header name is whatever the
     * configuration says.
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
