package org.owasp.oag.services.tokenMapping;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.config.configuration.UserMappingConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.owasp.oag.extension.TypedExtensionStub.stub;

class UserMappingValidatorTest {

    private final UserMappingValidator validator = new UserMappingValidator(
            new UserMappingRegistry(List.of(stub(UserMappingFactory.class, "no"))));

    private static MainConfig configWithUserMapping(String type) {
        var userMapping = new UserMappingConfig();
        userMapping.setType(type);
        var profile = new SecurityProfile();
        profile.setUserMapping(userMapping);
        return new MainConfig(Map.of(), Map.of(), new HashMap<>(Map.of("api", profile)), null, null, null, null, null);
    }

    @Test
    void knownTypeIsValid() {
        assertEquals(List.of(), validator.validate(configWithUserMapping("no")));
    }

    @Test
    void unknownTypeIsReportedWithSecurityProfile() {
        assertEquals(List.of("UserMapping of security profile 'api': Unknown type 'x'. Known types: [no]"),
                validator.validate(configWithUserMapping("x")));
    }
}
