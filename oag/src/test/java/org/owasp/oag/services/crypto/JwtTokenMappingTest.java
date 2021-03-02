package org.owasp.oag.services.crypto;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.login.drivers.UserModel;
import org.owasp.oag.services.tokenMapping.JwtTokenMapper;

import java.text.ParseException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenMappingTest {

    @Test
    void testStubMapping() throws ParseException {

        // Arrange
        var userId = "alice";
        var userEmail = "alice@example.com";
        var model = new UserModel(userId);
        model.getMappings().put("email", userEmail);

        var mappingSettings = new JwtTokenMapper.JwtTokenMappingSettings("Authorization", "Bearer", "Audience", "Issuer", 30, "stub", new HashMap<>());
        var mapper = new JwtTokenMapper(new StubJwtSigner(), new GlobalClockSource(), mappingSettings);

        // Act
        var jwt = mapper.mapUserModelToToken(model);

        // Assert
        var signedJwt = SignedJWT.parse(jwt);
        var claims = signedJwt.getJWTClaimsSet();
        assertEquals(userId, claims.getSubject());
        assertEquals(userEmail, claims.getClaim("email"));
    }
}