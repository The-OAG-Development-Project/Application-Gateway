package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the configured KeyManagement section in the config file.
 * This class manages the various components needed for cryptographic key management,
 * including JWK storage, key generation, and key rotation.
 */
public class KeyManagementProfile implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(KeyManagementProfile.class);

    /**
     * Profile configuration for the JWK (JSON Web Key) store.
     */
    private JwkStoreProfile jwkStoreProfile;
    
    /**
     * Profile configuration for the key generator.
     */
    private KeyGeneratorProfile keyGeneratorProfile;
    
    /**
     * Profile configuration for key rotation.
     */
    private KeyRotationProfile keyRotationProfile;

    /**
     * Default constructor.
     */
    public KeyManagementProfile() {
    }

    /**
     * Creates a new KeyManagementProfile with the specified parameters.
     *
     * @param jwkStoreProfile Profile configuration for the JWK store
     * @param keyGeneratorProfile Profile configuration for the key generator
     * @param keyRotationProfile Profile configuration for key rotation
     */
    public KeyManagementProfile(JwkStoreProfile jwkStoreProfile, KeyGeneratorProfile keyGeneratorProfile, KeyRotationProfile keyRotationProfile) {
        this.jwkStoreProfile = jwkStoreProfile;
        this.keyGeneratorProfile = keyGeneratorProfile;
        this.keyRotationProfile = keyRotationProfile;
    }

    /**
     * Gets the JWK store profile configuration.
     *
     * @return The JWK store profile
     */
    public JwkStoreProfile getJwkStoreProfile() {
        return jwkStoreProfile;
    }

    /**
     * Sets the JWK store profile configuration.
     *
     * @param jwkStoreProfile The JWK store profile
     */
    public void setJwkStoreProfile(JwkStoreProfile jwkStoreProfile) {
        this.jwkStoreProfile = jwkStoreProfile;
    }

    /**
     * Gets the key generator profile configuration.
     *
     * @return The key generator profile
     */
    public KeyGeneratorProfile getKeyGeneratorProfile() {
        return keyGeneratorProfile;
    }

    /**
     * Sets the key generator profile configuration.
     *
     * @param keyGeneratorProfile The key generator profile
     */
    public void setKeyGeneratorProfile(KeyGeneratorProfile keyGeneratorProfile) {
        this.keyGeneratorProfile = keyGeneratorProfile;
    }

    /**
     * Gets the key rotation profile configuration.
     *
     * @return The key rotation profile
     */
    public KeyRotationProfile getKeyRotationProfile() {
        return keyRotationProfile;
    }

    /**
     * Sets the key rotation profile configuration.
     *
     * @param keyRotationProfile The key rotation profile
     */
    public void setKeyRotationProfile(KeyRotationProfile keyRotationProfile) {
        this.keyRotationProfile = keyRotationProfile;
    }

    /**
     * Validates the key management profile configuration and returns a list of validation errors.
     * This method checks that all required profiles are present and valid.
     *
     * @param context The Spring application context, used for validation of the individual profiles
     * @return A list of error messages, empty if the configuration is valid
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (jwkStoreProfile == null)
            errors.add("'jwkStoreProfile' not specified in section keyManagementProfile");

        if (keyGeneratorProfile == null)
            errors.add("'keyGeneratorProfile' not specified in section keyManagementProfile");

        if (keyRotationProfile == null)
            errors.add("'keyRotationProfile' not specified in section keyManagementProfile");

        if (!errors.isEmpty())
            return errors;

        errors.addAll(jwkStoreProfile.getErrors(context));
        errors.addAll(keyGeneratorProfile.getErrors(context));
        errors.addAll(keyRotationProfile.getErrors(context));

        return errors;
    }
}
