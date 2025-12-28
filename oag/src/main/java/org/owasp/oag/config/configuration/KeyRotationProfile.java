package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the configured KeyRotationProfile in Section KeyManagementProfile of the configuration.
 * Key rotation is a security best practice that involves regularly changing cryptographic keys
 * to limit the amount of data encrypted with the same key and to reduce the impact of key compromise.
 */
public class KeyRotationProfile implements ErrorValidation {
    private static final Logger log = LoggerFactory.getLogger(KeyRotationProfile.class);

    /**
     * The type of key rotation implementation to use.
     * Must match a bean name of a key rotation implementation.
     */
    private String type;
    
    /**
     * Flag indicating whether signing key rotation is enabled.
     */
    private Boolean useSigningKeyRotation;
    
    /**
     * The frequency of signing key rotation in seconds.
     * Only used if useSigningKeyRotation is true.
     */
    private Integer signingKeyRotationSeconds;
    
    /**
     * The frequency of cleanup operations for old keys in seconds.
     */
    private Integer cleanupFrequencySeconds;

    /**
     * Default constructor.
     */
    public KeyRotationProfile() {
    }

    /**
     * Creates a new KeyRotationProfile with the specified parameters.
     *
     * @param type The type of key rotation implementation to use
     * @param useSigningKeyRotation Flag indicating whether signing key rotation is enabled
     * @param signingKeyRotationSeconds The frequency of signing key rotation in seconds
     * @param cleanupFrequencySeconds The frequency of cleanup operations for old keys in seconds
     */
    public KeyRotationProfile(String type, Boolean useSigningKeyRotation, Integer signingKeyRotationSeconds, Integer cleanupFrequencySeconds) {
        this.type = type;
        this.useSigningKeyRotation = useSigningKeyRotation;
        this.signingKeyRotationSeconds = signingKeyRotationSeconds;
        this.cleanupFrequencySeconds = cleanupFrequencySeconds;
    }

    /**
     * Gets the type of key rotation implementation to use.
     *
     * @return The type of key rotation implementation
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of key rotation implementation to use.
     *
     * @param type The type of key rotation implementation
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Checks if signing key rotation is enabled.
     *
     * @return true if signing key rotation is enabled, false otherwise
     */
    public Boolean getUseSigningKeyRotation() {
        return useSigningKeyRotation;
    }

    /**
     * Sets whether signing key rotation is enabled.
     *
     * @param useSigningKeyRotation true to enable signing key rotation, false to disable it
     */
    public void setUseSigningKeyRotation(Boolean useSigningKeyRotation) {
        this.useSigningKeyRotation = useSigningKeyRotation;
    }

    /**
     * Gets the frequency of signing key rotation in seconds.
     *
     * @return The frequency of signing key rotation in seconds
     */
    public Integer getSigningKeyRotationSeconds() {
        return signingKeyRotationSeconds;
    }

    /**
     * Sets the frequency of signing key rotation in seconds.
     *
     * @param signingKeyRotationSeconds The frequency of signing key rotation in seconds
     */
    public void setSigningKeyRotationSeconds(Integer signingKeyRotationSeconds) {
        this.signingKeyRotationSeconds = signingKeyRotationSeconds;
    }

    /**
     * Gets the frequency of cleanup operations for old keys in seconds.
     *
     * @return The frequency of cleanup operations in seconds
     */
    public Integer getCleanupFrequencySeconds() {
        return cleanupFrequencySeconds;
    }

    /**
     * Sets the frequency of cleanup operations for old keys in seconds.
     *
     * @param cleanupFrequencySeconds The frequency of cleanup operations in seconds
     */
    public void setCleanupFrequencySeconds(Integer cleanupFrequencySeconds) {
        this.cleanupFrequencySeconds = cleanupFrequencySeconds;
    }

    /**
     * Validates the key rotation profile configuration and returns a list of validation errors.
     * This method checks that all required settings are present and valid.
     *
     * @param context The Spring application context, used to verify that the specified type exists
     * @return A list of error messages, empty if the configuration is valid
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (useSigningKeyRotation == null)
            errors.add("'useSigningKeyRotation' not specified in section keyManagementProfile");
        else {
            if (useSigningKeyRotation && (signingKeyRotationSeconds == null || signingKeyRotationSeconds < 1))
                errors.add("'signingKeyRotationHours' not specified in section keyManagementProfile or value <1 (must be 1 or more).");
        }

        if (cleanupFrequencySeconds == null)
            errors.add("'cleanupFrequencySeconds' not specified in section keyManagementProfile. Recommended to be at least 10 minutes, up to a few days, consider signingKeyRotation to be a 'reasonable upper bound'.");

        if (type == null)
            errors.add("'type' not specified. Must be the bean name of a bean that initializes the signing key on creation. Usually you should use 'defaultKeyRotation'.");

        if (context != null && !context.containsBean(type)) {
            errors.add("Specified type '" + type + "' does not match a bean. Must be the bean name of a bean that initializes the signing key on creation. Usually you should use 'defaultKeyRotation'.");
        } else {
            log.info("Using key rotation implementation of bean {}.", type);
        }

        if (!errors.isEmpty())
            return errors;

        return errors;
    }
}
