package org.owasp.oag.config.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the configured keyGeneratorProfile section in the config file.
 * This class holds settings for generating cryptographic keys used in the application.
 */
public class KeyGeneratorProfile implements ErrorValidation {

    /**
     * The type of key generator to use. Must match the type of a registered KeyGenerator.
     */
    private String type;
    
    /**
     * The size of the key to generate in bits.
     */
    private Integer keySize;
    
    /**
     * Implementation-specific settings for the key generator.
     */
    private Map<String, Object> implSpecificSettings = new HashMap<>();

    /**
     * Default constructor.
     */
    public KeyGeneratorProfile() {
    }

    /**
     * Creates a new KeyGeneratorProfile with the specified parameters.
     *
     * @param type The type of key generator to use
     * @param keySize The size of the key to generate in bits
     * @param implSpecificSettings Implementation-specific settings for the key generator
     */
    public KeyGeneratorProfile(String type, Integer keySize, Map<String, Object> implSpecificSettings) {
        this.type = type;
        this.keySize = keySize;
        this.implSpecificSettings = implSpecificSettings;
    }

    /**
     * Gets the type of key generator to use.
     *
     * @return The type of key generator
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of key generator to use.
     *
     * @param type The type of key generator
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the size of the key to generate in bits.
     *
     * @return The key size in bits
     */
    public Integer getKeySize() {
        return keySize;
    }

    /**
     * Sets the size of the key to generate in bits.
     *
     * @param keySize The key size in bits
     */
    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    /**
     * Gets the implementation-specific settings for the key generator.
     *
     * @return Map of implementation-specific settings
     */
    public Map<String, Object> getImplSpecificSettings() {
        return implSpecificSettings;
    }

    /**
     * Sets the implementation-specific settings for the key generator.
     *
     * @param implSpecificSettings Map of implementation-specific settings
     */
    public void setTraceImplSpecificSettings(Map<String, Object> implSpecificSettings) {
        this.implSpecificSettings = implSpecificSettings;
    }

    /**
     * Validates the key generator profile configuration and returns a list of validation errors.
     *
     * @return A list of error messages, empty if the configuration is valid
     */
    @Override
    public List<String> getErrors() {
        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("'type' not specified. Use e.g. 'rsaKeyGenerator'.");

        if (keySize == null || keySize < 64)
            errors.add("'keySize' not specified or way to short.");

        return errors;
    }
}
