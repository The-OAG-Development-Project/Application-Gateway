package org.owasp.oag.services.tokenMapping.header;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings class for header-based user mapping
 * Contains configuration for mapping user attributes to HTTP headers
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestHeaderUserMappingSettings {

    /**
     * Map of header names to template expressions for user attributes
     * Keys are header names, values are template expressions to be evaluated
     */
    public Map<String, String> mappings = new HashMap<>();

    /**
     * Validates that all mapping templates are correctly formatted
     * Throws a ConfigurationException if any template is invalid
     */
    public void requireValidSettings() {

        for (var entry : mappings.entrySet()) {
            if (!UserMappingTemplatingEngine.isValidTemplate(entry.getValue())) {
                throw new ConfigurationException("Invalid mapping in RequestHeaderUserMappingSettings: " + entry.getKey() + " -> " + entry.getValue(), null);
            }
        }
    }
}
