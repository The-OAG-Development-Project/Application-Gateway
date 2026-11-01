package org.owasp.oag.services.tokenMapping;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.config.configuration.UserMappingConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.tokenMapping.nouser.NoUserMapper;
import org.owasp.oag.services.tokenMapping.nouser.NoUserMappingFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityProfileUserMappersTest {

    private final UserMappingRegistry registry = new UserMappingRegistry(List.of(new NoUserMappingFactory()));

    @Test
    void createsOneMapperPerSecurityProfile() {
        var mappers = new SecurityProfileUserMappers(Map.of("a", profile("no"), "b", profile("no")), registry);

        assertInstanceOf(NoUserMapper.class, mappers.get("a"));
        assertNotSame(mappers.get("a"), mappers.get("b"));
    }

    @Test
    void unknownSecurityProfileThrows() {
        var mappers = new SecurityProfileUserMappers(Map.of("a", profile("no")), registry);

        assertThrows(ConfigurationException.class, () -> mappers.get("x"));
    }

    @Test
    void unknownUserMappingTypeThrowsRegistryMessage() {
        var profiles = Map.of("a", profile("unknown"));

        var ex = assertThrows(ConfigurationException.class, () -> new SecurityProfileUserMappers(profiles, registry));

        assertTrue(ex.getMessage().contains("'unknown'"), ex.getMessage());
        assertTrue(ex.getMessage().contains("[no]"), ex.getMessage());
    }

    private static SecurityProfile profile(String userMappingType) {
        var userMapping = new UserMappingConfig();
        userMapping.setType(userMappingType);
        userMapping.setSettings(Map.of());
        var profile = new SecurityProfile();
        profile.setUserMapping(userMapping);
        return profile;
    }
}
