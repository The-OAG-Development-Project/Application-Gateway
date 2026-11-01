package org.owasp.oag.services.crypto.jwt;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtSignerRegistryTest {

    @Test
    void registersBuiltInSignatureImplementations() {
        var registry = new JwtSignerRegistry(List.of(new RsaJwtSignerFactory(), new HmacJwtSignerFactory()));

        assertEquals(Set.of("rsa", "hmac"), registry.types());
    }
}
