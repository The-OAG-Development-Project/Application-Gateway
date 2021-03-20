package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.config.InvalidOAGSettingsException;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;

@Component("header-mapping" + USER_MAPPER_TYPE_POSTFIX)
public class RequestHeaderUserMappingFactory implements UserMappingFactory {

    @Override
    public UserMapper load(Map<String, Object> settings) throws InvalidOAGSettingsException{

        // Load settings
        RequestHeaderUserMappingSettings mappingSettings;
        try{
            mappingSettings = SettingsUtils.settingsFromMap(settings, RequestHeaderUserMappingSettings.class);
        }
        catch(Exception ex){
            throw new InvalidOAGSettingsException("Cannot deserialize header-mapping settings", ex);
        }
        mappingSettings.requireValidSettings();

        return new RequestHeaderUserMapping(mappingSettings);
    }
}
