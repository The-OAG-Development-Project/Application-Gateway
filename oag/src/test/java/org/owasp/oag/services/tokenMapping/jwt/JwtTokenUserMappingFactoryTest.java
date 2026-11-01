package org.owasp.oag.services.tokenMapping.jwt;

import org.junit.jupiter.api.Test;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.crypto.jwt.HmacJwtSignerFactory;
import org.owasp.oag.services.crypto.jwt.JwtSignerRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUserMappingFactoryTest {

    @Test
    void unknownSignatureImplementationListsKnownTypes() {
        var factory = new JwtTokenUserMappingFactory(new JwtSignerRegistry(List.of(new HmacJwtSignerFactory())), null, null);
        Map<String, Object> settings = Map.of(
                "headerName", "Authorization",
                "headerPrefix", "Bearer ",
                "audience", "aud",
                "issuer", "iss",
                "signatureImplementation", "doesnotexist");

        var ex = assertThrows(ConfigurationException.class, () -> factory.load(settings));

        assertTrue(ex.getMessage().contains("'doesnotexist'"), ex.getMessage());
        assertTrue(ex.getMessage().contains("[hmac]"), ex.getMessage());
    }
}
