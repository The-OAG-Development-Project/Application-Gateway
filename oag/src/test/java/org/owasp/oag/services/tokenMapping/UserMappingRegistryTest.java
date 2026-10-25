package org.owasp.oag.services.tokenMapping;

import org.junit.jupiter.api.Test;
import org.owasp.oag.services.tokenMapping.header.RequestHeaderUserMappingFactory;
import org.owasp.oag.services.tokenMapping.jwt.JwtTokenUserMappingFactory;
import org.owasp.oag.services.tokenMapping.nouser.NoUserMappingFactory;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMappingRegistryTest {

    @Test
    void registersBuiltInUserMappingTypes() {
        var registry = new UserMappingRegistry(List.of(
                new NoUserMappingFactory(), new RequestHeaderUserMappingFactory(), new JwtTokenUserMappingFactory(null, null, null)));

        assertEquals(Set.of("no", "requestHeader", "jwtToken"), registry.types());
    }
}
