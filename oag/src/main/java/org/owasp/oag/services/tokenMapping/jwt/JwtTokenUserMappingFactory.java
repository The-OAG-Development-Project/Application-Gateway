package org.owasp.oag.services.tokenMapping.jwt;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.jwt.JwtSignerFactory;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.crypto.jwt.JwtSignerFactory.JWT_SIGNER_FACTORY_BEAN_POSTFIX;

@Component
public class JwtTokenUserMappingFactory implements UserMappingFactory {

    @Autowired
    ApplicationContext context;

    @Autowired
    GlobalClockSource clockSource;

    @Autowired
    MainConfig mainConfig;

    @Override
    public UserMapper load(Map<String, Object> settings) {

        // Load settings
        JwtTokenUserMappingSettings jwtTokenUserMappingSettings;
        try {
            jwtTokenUserMappingSettings = SettingsUtils.settingsFromMap(settings, JwtTokenUserMappingSettings.class);
        } catch (Exception ex) {
            throw new ConfigurationException("Cannot deserialize jwt-mapping settings", ex);
        }
        jwtTokenUserMappingSettings.requireValidSettings();

        // Init jwt signer
        var factoryName = jwtTokenUserMappingSettings.signatureImplementation + JWT_SIGNER_FACTORY_BEAN_POSTFIX;
        try {
            var factory = context.getBean(factoryName, JwtSignerFactory.class);
            // Create user mapper
            return new JwtTokenUserMapper(factory, clockSource, jwtTokenUserMappingSettings, mainConfig.getHostUri());
        } catch (BeansException be) {
            throw new ConfigurationException("No implementation found for signature implementation: " + jwtTokenUserMappingSettings.signatureImplementation+". The Bean is expected to be named: "+factoryName, be);
        }
    }
}
