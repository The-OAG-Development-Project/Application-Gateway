package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.services.keymgm.JwkStoreRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the configured jwkStoreProfile section in the config file.
 * JWK (JSON Web Key) store is responsible for storing and retrieving cryptographic
 * keys used for signing JWT tokens.
 */
public class JwkStoreProfile implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(JwkStoreProfile.class);

    /**
     * The type of JWK store implementation to use.
     * Must match a bean name of a JwkStore implementation.
     */
    private String type;
    
    /**
     * Implementation-specific settings for the JWK store.
     */
    private Map<String, Object> implSpecificSettings = new HashMap<>();

    /**
     * Default constructor.
     */
    public JwkStoreProfile() {
    }

    /**
     * Creates a new JwkStoreProfile with the specified parameters.
     *
     * @param type The type of JWK store to use
     * @param implSpecificSettings Implementation-specific settings for the JWK store
     */
    public JwkStoreProfile(String type, Map<String, Object> implSpecificSettings) {
        this.type = type;
        this.implSpecificSettings = implSpecificSettings;
    }

    /**
     * Gets the type of JWK store to use.
     *
     * @return The type of JWK store
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of JWK store to use.
     *
     * @param type The type of JWK store
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the implementation-specific settings for the JWK store.
     *
     * @return Map of implementation-specific settings
     */
    public Map<String, Object> getImplSpecificSettings() {
        return implSpecificSettings;
    }

    /**
     * Sets the implementation-specific settings for the JWK store.
     *
     * @param implSpecificSettings Map of implementation-specific settings
     */
    public void setTraceImplSpecificSettings(Map<String, Object> implSpecificSettings) {
        this.implSpecificSettings = implSpecificSettings;
    }

    /**
     * Validates the JWK store profile configuration and returns a list of validation errors.
     *
     * @param context The Spring application context, used to look up the registered types
     * @return A list of error messages, empty if the configuration is valid
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("'type' not specified. Use e.g. 'localRsaJwkStore'.");

        if (context != null && type != null) {
            var registry = context.getBean(JwkStoreRegistry.class);
            if (!registry.contains(type))
                errors.add("JwkStore: Unknown type '" + type + "'. Known types: " + registry.types());
            else
                log.info("Using JwkStore implementation of {}.", type);
        }

        return errors;
    }
}
