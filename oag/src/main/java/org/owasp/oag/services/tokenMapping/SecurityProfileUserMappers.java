package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.exception.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds one {@link UserMapper} per used security profile, created at startup.
 */
@Component
public class SecurityProfileUserMappers {

    private final Map<String, UserMapper> mappers = new HashMap<>();

    /**
     * Creates the user mappers of all security profiles used by a route.
     *
     * @param config   The main configuration
     * @param registry The user mapping registry
     */
    @Autowired
    public SecurityProfileUserMappers(MainConfig config, UserMappingRegistry registry) {
        this(config.getUsedSecurityProfiles(), registry);
    }

    /**
     * Creates the user mappers of the given security profiles.
     *
     * @param profiles The security profiles by name
     * @param registry The user mapping registry
     * @throws ConfigurationException If a user mapping type is unknown or its settings are invalid
     */
    public SecurityProfileUserMappers(Map<String, SecurityProfile> profiles, UserMappingRegistry registry) {
        profiles.forEach((name, profile) -> {
            var userMapping = profile.getUserMapping();
            mappers.put(name, registry.get(userMapping.getType()).load(userMapping.getSettings()));
        });
    }

    /**
     * Returns the user mapper of a security profile.
     *
     * @param securityProfileName The name of the security profile
     * @return The user mapper
     * @throws ConfigurationException If the security profile is not used by any route
     */
    public UserMapper get(String securityProfileName) {
        var mapper = mappers.get(securityProfileName);
        if (mapper == null)
            throw new ConfigurationException("UserMapper not found: Unexpected securityProfile name: " + securityProfileName);
        return mapper;
    }
}
