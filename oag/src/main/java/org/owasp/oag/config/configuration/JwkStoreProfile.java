package org.owasp.oag.config.configuration;

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

    /**
     * The type of JWK store implementation to use.
     * Must match the type of a registered JwkStore.
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
     * @return A list of error messages, empty if the configuration is valid
     */
    @Override
    public List<String> getErrors() {

        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("'type' not specified. Use e.g. 'localRsaJwkStore'.");

        return errors;
    }
}
