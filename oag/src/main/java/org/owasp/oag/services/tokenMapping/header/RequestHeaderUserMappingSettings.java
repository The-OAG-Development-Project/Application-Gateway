package org.owasp.oag.services.tokenMapping.header;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.owasp.oag.config.InvalidOAGSettingsException;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestHeaderUserMappingSettings {

    public Map<String, String> mappings = new HashMap<>();

    public void requireValidSettings() throws InvalidOAGSettingsException {

        for(var entry : mappings.entrySet())
        {
            if(!UserMappingTemplatingEngine.isValidTemplate(entry.getValue())){

                throw new InvalidOAGSettingsException("Invalid mapping in RequestHeaderUserMappingSettings: " + entry.getKey() + " -> " + entry.getValue());
            }
        }
    }
}
