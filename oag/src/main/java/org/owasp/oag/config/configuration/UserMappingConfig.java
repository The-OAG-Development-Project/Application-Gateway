package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.services.tokenMapping.UserMappingRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for user mapping.
 * Defines the type of user mapping to use and its settings.
 * Implements ErrorValidation to validate the configuration.
 */
public class UserMappingConfig implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(UserMappingConfig.class);

    private String type;
    private Map<String, Object> settings;

    /**
     * Gets the type of user mapping.
     *
     * @return The type of user mapping
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of user mapping.
     *
     * @param type The type of user mapping
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the settings for the user mapping.
     *
     * @return The settings as a map of key-value pairs
     */
    public Map<String, Object> getSettings() {
        return settings;
    }

    /**
     * Sets the settings for the user mapping.
     *
     * @param settings The settings as a map of key-value pairs
     */
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (context == null)
            return errors;

        if (this.type == null)
            errors.add("Config: tokenMapping implementation is not defined");

        var registry = context.getBean(UserMappingRegistry.class);
        if (!registry.contains(this.type)) {
            errors.add("UserMapping: Unknown type '" + this.type + "'. Known types: " + registry.types());
        } else {
            log.debug("Using token mapping implementation of {}.", this.type);
        }

        return errors;
    }
}
