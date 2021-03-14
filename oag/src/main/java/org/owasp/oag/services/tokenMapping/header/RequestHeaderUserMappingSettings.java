package org.owasp.oag.services.tokenMapping.header;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.owasp.oag.services.tokenMapping.UserMapperUtils;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestHeaderUserMappingSettings {

    public Map<String, String> mappings = new HashMap<>();

    public void requireValidSettings() {

        mappings.entrySet().forEach((entry) -> {

            if(!UserMapperUtils.isValidMapping(entry.getValue())){

                throw new RuntimeException("Invalid mapping in RequestHeaderUserMappingSettings: " + entry.getKey() + " -> " + entry.getValue());
            }
        });
    }
}
