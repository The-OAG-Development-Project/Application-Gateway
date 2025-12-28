package org.owasp.oag.config.configuration;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides default configuration values for various aspects of the application.
 * This class contains static methods that return default configurations when not explicitly provided.
 */
public class DefaultConfigValues {

    /**
     * Provides a default user mapping configuration.
     * 
     * @return A UserMappingConfig object with default settings (type "no" and empty settings)
     */
    public static UserMappingConfig userMapping() {

        var userMapping = new UserMappingConfig();
        userMapping.setType("no");
        userMapping.setSettings(new HashMap<>());
        return userMapping;
    }

    /**
     * Provides a default list of HTTP methods that are considered safe from CSRF attacks.
     * 
     * @return A list containing "GET", "HEAD", and "OPTIONS" methods
     */
    public static List<String> csrfSafeMethods() {

        return Lists.asList("GET", new String[]{"HEAD", "OPTIONS"});
    }

    /**
     * Provides a default map of response headers.
     * 
     * @return An empty map of response headers
     */
    public static Map<String, String> responseHeaders() {

        return new HashMap<>();
    }
}
