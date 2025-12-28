package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.tokenMapping.UserMapper;

/**
 * Factory interface for retrieving user mappers for security profiles.
 * This interface defines methods to obtain the appropriate user mapper
 * for a given security profile.
 */
public interface UserMappingFactory {

    /**
     * Retrieves the user mapper for the specified security profile.
     *
     * @param securityProfileName The name of the security profile
     * @return The user mapper for the specified security profile
     */
    UserMapper getUserMapperForSecurityProfile(String securityProfileName);
}
