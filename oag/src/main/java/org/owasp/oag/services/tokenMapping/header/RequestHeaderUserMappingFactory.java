package org.owasp.oag.services.tokenMapping.header;

import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RequestHeaderUserMappingFactory implements UserMappingFactory {

    @Override
    public UserMapper load(Map<String, Object> settings) {

        // Load settings
        RequestHeaderUserMappingSettings mappingSettings;
        try {
            mappingSettings = SettingsUtils.settingsFromMap(settings, RequestHeaderUserMappingSettings.class);
        } catch (Exception ex) {
            throw new ConfigurationException("Cannot deserialize header-mapping settings", ex);
        }
        mappingSettings.requireValidSettings();

        return new RequestHeaderUserMapper(mappingSettings);
    }
}
