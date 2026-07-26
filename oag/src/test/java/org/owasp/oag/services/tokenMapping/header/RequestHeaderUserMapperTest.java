package org.owasp.oag.services.tokenMapping.header;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the header user mapper replaces a client supplied value of a mapped header instead of
 * appending to it. Appending would leave the attacker's value in front of OAG's for any downstream system
 * that reads the first value of the header.
 */
class RequestHeaderUserMapperTest {

    private static final String USER_HEADER = RequestHeaderUserMapper.X_OAG_USER_PREFIX + "ID";

    @Test
    void replacesAClientSuppliedMappedHeaderInsteadOfAppendingToIt() {

        // Arrange
        var settings = new RequestHeaderUserMappingSettings();
        settings.mappings.put(USER_HEADER, "<session.userModel.id>");
        var mapper = new RequestHeaderUserMapper(settings);

        var servletRequest = new MockHttpServletRequest("GET", "/api/foo");
        servletRequest.addHeader(USER_HEADER, "administrator");
        var request = ServerRequest.create(servletRequest, List.<HttpMessageConverter<?>>of(new StringHttpMessageConverter()));

        // Act
        var mapped = mapper.mapUserToRequest(request, routeContext("alice"));

        // Assert
        assertEquals(List.of("alice"), mapped.headers().header(USER_HEADER),
                "the client supplied user header must not survive next to OAG's");
    }

    private GatewayRouteContext routeContext(String userId) {

        var route = new GatewayRoute("/api/**", "https://backend.com", "type", true, null);
        var session = new Session(300, 300, "IAM", new UserModel(userId), null, "sessionId");
        return new GatewayRouteContext("routeName", route, null, "/api/foo", "/api/foo", Optional.of(session));
    }
}
