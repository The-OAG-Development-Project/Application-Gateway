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


@Component
public class DefaultUserMappingFactory implements UserMappingFactory {

    private final Map<String, UserMapper> mapperMap;

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

    @Override
    public UserMapper getUserMapperForSecurityProfile(String securityProfileName) {
        if (!mapperMap.containsKey(securityProfileName))
            throw new ConfigurationException("UserMapper not found: Unexpected securityProfile name: " + securityProfileName, null);

        return mapperMap.get(securityProfileName);
    }
}
