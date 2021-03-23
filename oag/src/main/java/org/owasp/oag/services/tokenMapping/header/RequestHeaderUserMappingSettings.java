package org.owasp.oag.services.tokenMapping.header;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestHeaderUserMappingSettings {

    public Map<String, String> mappings = new HashMap<>();

    public void requireValidSettings() {

        for (var entry : mappings.entrySet()) {
            if (!UserMappingTemplatingEngine.isValidTemplate(entry.getValue())) {
                throw new ConfigurationException("Invalid mapping in RequestHeaderUserMappingSettings: " + entry.getKey() + " -> " + entry.getValue(), null);
            }
        }
    }
}
