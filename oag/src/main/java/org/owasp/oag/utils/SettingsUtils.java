package org.owasp.oag.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.owasp.oag.exception.ConfigurationException;

import java.util.Map;

/**
 * Utility class for handling application settings and configuration.
 * Provides methods for converting between different settings representations
 * such as maps and strongly-typed configuration objects.
 */
public class SettingsUtils {

    /**
     * Converts a map of settings to a strongly-typed configuration object.
     * This method uses Jackson to serialize the map to YAML and then deserialize it
     * into an instance of the specified class.
     *
     * @param <T> The type of configuration object to create
     * @param map The map containing settings key-value pairs
     * @param clazz The class representing the configuration type
     * @return An instance of the specified class populated with settings from the map
     * @throws ConfigurationException If the settings cannot be processed or are invalid
     */
    public static <T> T settingsFromMap(Map<String, Object> map, Class<T> clazz) {

        try {
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            String combinedConfigStr = om.writeValueAsString(map);
            return om.readValue(combinedConfigStr, clazz);
        } catch (JsonProcessingException ex) {

            throw new ConfigurationException("Could not load settings", ex);
        }
    }
}
