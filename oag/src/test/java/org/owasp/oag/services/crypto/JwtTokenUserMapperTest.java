package org.owasp.oag.services.crypto;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.jwt.StubJwtSignerFactory;
import org.owasp.oag.services.tokenMapping.jwt.JwtTokenUserMapper;
import org.owasp.oag.services.tokenMapping.jwt.JwtTokenUserMappingSettings;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtTokenUserMapperTest {

    private final String hostUri = "https://gateway";
    private final String routeUrl = "https://backend.com";
    private final String userId = "alice";
    private final String userEmail = "alice@example.com";
    private final String userPhone = "000 000 789";
    private final String provider = "IAM";

    private final UserModel model;
    private final GatewayRouteContext routeContext;

    public JwtTokenUserMapperTest() {

        // User Model
        model = new UserModel(userId);
        model.getMappings().put("email", userEmail);
        model.getMappings().put("phone", userPhone);

        var route = new GatewayRoute("/api/**", routeUrl, "type", true, null);
        var session = new Session(300, 300, provider, model, null, "sessionId");
        routeContext = new GatewayRouteContext("routeName", route, null, "https://request/uri", "https://upstream/url", Optional.of(session));
    }

    @Test
    void testTokenMapping() throws Exception {

        // Arrange
        var mappingSettingsMappings = new HashMap<String, String>();
        mappingSettingsMappings.put("email-claim", "<mappings.email>");
        mappingSettingsMappings.put("constant-claim", "abc");
        mappingSettingsMappings.put("provider", "<session.provider>");
        var mappingSettings = new JwtTokenUserMappingSettings("Authorization", "Bearer", "<<route-url>>", "<<hostUri>>", 30, "stub", new HashMap<>(), mappingSettingsMappings);
        var mapper = new JwtTokenUserMapper(new StubJwtSignerFactory(), new GlobalClockSource(), mappingSettings, hostUri);

        // Act
        var jwt = mapper.mapUserModelToToken(routeContext, routeUrl, provider);

        // Assert
        var signedJwt = SignedJWT.parse(jwt);
        var claims = signedJwt.getJWTClaimsSet();
        assertEquals(userId, claims.getSubject());
        assertEquals(routeUrl, claims.getAudience().get(0));
        assertEquals(hostUri, claims.getIssuer());
        assertEquals(userEmail, claims.getClaim("email-claim"));
        assertEquals("abc", claims.getClaim("constant-claim"));
        assertNull(claims.getClaim("phone"));
        assertEquals(provider, claims.getClaim("provider"));
    }

    @Test
    void testTokenMappingCache() {

        // Arrange
        var clockSource = new GlobalClockSource();
        var mappingSettings = new JwtTokenUserMappingSettings("Authorization", "Bearer", "<<route-url>>", "<<hostUri>>", 30, "stub", new HashMap<>(), new HashMap<>());
        var mapper = new JwtTokenUserMapper(new StubJwtSignerFactory(), clockSource, mappingSettings, hostUri);
        var provider = "iam";

        // Act
        var jwt1 = mapper.getTokenMono(model, routeUrl, provider, null, routeContext).block();
        var jwt2 = mapper.getTokenMono(model, routeUrl, provider, null, routeContext).block();

        // Assert
        assertEquals(jwt1, jwt2, "Second jwt should be loaded from cache and therefore be equal");
    }
}