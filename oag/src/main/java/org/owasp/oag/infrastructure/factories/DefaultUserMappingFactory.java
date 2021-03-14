package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;

@Component
public class DefaultUserMappingFactory implements UserMappingFactory{

    private Map<String, UserMapper> mapperMap;

    @Autowired
    public DefaultUserMappingFactory(MainConfig config, ApplicationContext context) {

        // Get all security profiles that are actually used
        var usedProfiles = config.getUsedSecurityProfiles();
        this.mapperMap = new HashMap<>();

        // Init the user mapper for each of it
        usedProfiles.forEach((profileName, profile) -> initUserMapper(profileName, profile, context));
    }

    private void initUserMapper(String profileName, SecurityProfile profile, ApplicationContext context){

        var userMappingSettings = profile.getUserMapping();
        var userMappingType = userMappingSettings.getType();
        var factoryName = userMappingType + USER_MAPPER_TYPE_POSTFIX;
        var factory = context.getBean(factoryName, org.owasp.oag.services.tokenMapping.UserMappingFactory.class);

        if (factory == null) {
            throw new RuntimeException("Cannot find factory for UserMapper of type '" + userMappingType +"'");
        }

        var userMapper = factory.load(userMappingSettings.getSettings());
        mapperMap.put(profileName, userMapper);
    }

    @Override
    public UserMapper getUserMapperForSecurityProfile(String securityProfileName) {

        if(! mapperMap.containsKey(securityProfileName))
            throw new RuntimeException("UserMapper not found: Unexpected securityProfile name: " + securityProfileName);

        return mapperMap.get(securityProfileName);
    }
}
