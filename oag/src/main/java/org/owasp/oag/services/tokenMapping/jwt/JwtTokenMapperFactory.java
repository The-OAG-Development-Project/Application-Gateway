package org.owasp.oag.services.tokenMapping.jwt;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.JwtSigner;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;

@Component("jwt-mapping" + USER_MAPPER_TYPE_POSTFIX)
public class JwtTokenMapperFactory implements UserMappingFactory {

    @Autowired
    ApplicationContext context;

    @Autowired
    GlobalClockSource clockSource;

    @Autowired
    MainConfig mainConfig;

    @Override
    public UserMapper load(Map<String, Object> settings) {

        // Load settings
        JwtTokenMappingSettings jwtTokenMappingSettings;
        try{
            jwtTokenMappingSettings = SettingsUtils.settingsFromMap(settings, JwtTokenMappingSettings.class);
        }
        catch(Exception ex){
            throw new RuntimeException("Cannot deserialize jwt-mapping settings", ex);
        }
        jwtTokenMappingSettings.requireValidSettings();

        // Init jwt signer
        var name = jwtTokenMappingSettings.signatureImplementation + "-jwt-signer";
        var signer = context.getBean(name, JwtSigner.class);

        return new JwtTokenMapper(signer, clockSource, jwtTokenMappingSettings, mainConfig.getHostUri());

    }
}
