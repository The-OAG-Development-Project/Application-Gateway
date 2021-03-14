package org.owasp.oag.services.crypto;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.tokenMapping.jwt.JwtTokenMapper;
import org.owasp.oag.services.tokenMapping.jwt.JwtTokenMappingSettings;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenMappingTest {

    private String hostUri = "https://gateway";
    private String routeUrl = "https://backend.com";
    private String userId = "alice";
    private String userEmail = "alice@example.com";
    private String userPhone = "000 000 789";

    private UserModel model;
    private GatewayRouteContext routeContext;

    public JwtTokenMappingTest() {

        // User Model
        model = new UserModel(userId);
        model.getMappings().put("email", userEmail);
        model.getMappings().put("phone", userPhone);

        var route = new GatewayRoute("/api/**", routeUrl, "type", true);
        var session = new Session(300, 300, "provider", model, null, "sessionId");
        routeContext = new GatewayRouteContext("routeName", route, null, "https://request/uri", "https://upstream/url", Optional.of(session));
    }

    @Test
    void testTokenMapping() throws ParseException {

        // Arrange
        var mappingSettingsMappings = new HashMap<String, String>();
        mappingSettingsMappings.put("email-claim", "userModel:email");
        mappingSettingsMappings.put("constant-claim", "constant:abc");
        var mappingSettings = new JwtTokenMappingSettings("Authorization", "Bearer", "<<route-url>>", "<<hostUri>>", 30, "stub", new HashMap<>(), mappingSettingsMappings);
        var mapper = new JwtTokenMapper(new StubJwtSigner(), new GlobalClockSource(), mappingSettings, hostUri);
        var provider = "iam";

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
        assertEquals(null, claims.getClaim("phone"));
        assertEquals(provider, claims.getClaim("provider"));
    }

    @Test
    void testTokenMappingCache(){

        // Arrange
        var clockSource = new GlobalClockSource();
        var mappingSettings = new JwtTokenMappingSettings("Authorization", "Bearer", "<<route-url>>", "<<hostUri>>", 30, "stub", new HashMap<>(), new HashMap<>());
        var mapper = new JwtTokenMapper(new StubJwtSigner(), clockSource, mappingSettings, hostUri);
        var provider = "iam";

        // Act
        var jwt1 = mapper.getTokenMono(model, routeUrl, provider, null, routeContext).block();
        var jwt2 = mapper.getTokenMono(model, routeUrl, provider, null, routeContext).block();

        // Assert
        assertEquals(jwt1, jwt2, "Second jwt should be loaded from cache and therefore be equal");
    }
}