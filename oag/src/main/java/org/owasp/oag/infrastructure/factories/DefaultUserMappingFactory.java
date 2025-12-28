package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;


/**
 * Default implementation of the UserMappingFactory interface.
 * This factory is responsible for initializing and managing user mappers for different security profiles.
 * It creates and caches user mappers for each security profile defined in the configuration.
 */
@Component
public class DefaultUserMappingFactory implements UserMappingFactory {

    /**
     * Map that stores user mappers for each security profile, keyed by profile name.
     */
    private final Map<String, UserMapper> mapperMap;

    /**
     * Constructs a new DefaultUserMappingFactory and initializes user mappers for all security profiles
     * defined in the configuration.
     *
     * @param config The main configuration containing security profile definitions
     * @param context The Spring application context used to retrieve user mapping factories
     */
    @Autowired
    public DefaultUserMappingFactory(MainConfig config, ApplicationContext context) {

        // Get all security profiles that are actually used
        var usedProfiles = config.getUsedSecurityProfiles();
        this.mapperMap = new HashMap<>();

        // Init the user mapper for each of it
        for (var entry : usedProfiles.entrySet()) {
            String profileName = entry.getKey();
            SecurityProfile profile = entry.getValue();
            initUserMapper(profileName, profile, context);
        }
    }

    /**
     * Initializes a user mapper for a specific security profile and adds it to the mapper map.
     *
     * @param profileName The name of the security profile
     * @param profile The security profile configuration
     * @param context The Spring application context used to retrieve user mapping factories
     * @throws ConfigurationException if the user mapping factory for the specified type cannot be found
     */
    private void initUserMapper(String profileName, SecurityProfile profile, ApplicationContext context) {
        var userMappingSettings = profile.getUserMapping();
        var userMappingType = userMappingSettings.getType();
        var factoryName = userMappingType + USER_MAPPER_TYPE_POSTFIX;
        try {
            var factory = context.getBean(factoryName, org.owasp.oag.services.tokenMapping.UserMappingFactory.class);
            var userMapper = factory.load(userMappingSettings.getSettings());
            mapperMap.put(profileName, userMapper);
        } catch (BeansException be){
            throw new ConfigurationException("Cannot find factory for UserMapper of type '" + userMappingType + "'. It is expected to be a Bean named: "+factoryName, null);
        }
    }

    /**
     * Retrieves the user mapper for the specified security profile.
     *
     * @param securityProfileName The name of the security profile
     * @return The user mapper for the specified security profile
     * @throws ConfigurationException if no user mapper is found for the specified security profile
     */
    @Override
    public UserMapper getUserMapperForSecurityProfile(String securityProfileName) {
        if (!mapperMap.containsKey(securityProfileName))
            throw new ConfigurationException("UserMapper not found: Unexpected securityProfile name: " + securityProfileName, null);

        return mapperMap.get(securityProfileName);
    }
}
