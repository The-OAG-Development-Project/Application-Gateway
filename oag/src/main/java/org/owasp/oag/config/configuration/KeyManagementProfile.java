package org.owasp.oag.config.configuration;

import org.owasp.oag.config.Subconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the configured KeyManagement section in the config file
 */
public class KeyManagementProfile implements Subconfig {

    private static final Logger log = LoggerFactory.getLogger(KeyManagementProfile.class);

    private JwkStoreProfile jwkStoreProfile;
    private KeyGeneratorProfile keyGeneratorProfile;
    private KeyRotationProfile keyRotationProfile;

    public KeyManagementProfile() {
    }

    public KeyManagementProfile(JwkStoreProfile jwkStoreProfile, KeyGeneratorProfile keyGeneratorProfile, KeyRotationProfile keyRotationProfile) {
        this.jwkStoreProfile = jwkStoreProfile;
        this.keyGeneratorProfile = keyGeneratorProfile;
        this.keyRotationProfile = keyRotationProfile;
    }

    public JwkStoreProfile getJwkStoreProfile() {
        return jwkStoreProfile;
    }

    public void setJwkStoreProfile(JwkStoreProfile jwkStoreProfile) {
        this.jwkStoreProfile = jwkStoreProfile;
    }

    public KeyGeneratorProfile getKeyGeneratorProfile() {
        return keyGeneratorProfile;
    }

    public void setKeyGeneratorProfile(KeyGeneratorProfile keyGeneratorProfile) {
        this.keyGeneratorProfile = keyGeneratorProfile;
    }

    public KeyRotationProfile getKeyRotationProfile() {
        return keyRotationProfile;
    }

    public void setKeyRotationProfile(KeyRotationProfile keyRotationProfile) {
        this.keyRotationProfile = keyRotationProfile;
    }

    @Override
    public List<String> getErrors(ApplicationContext context, MainConfig rootConfig) {

        var errors = new ArrayList<String>();

        if (jwkStoreProfile == null)
            errors.add("'jwkStoreProfile' not specified in section keyManagementProfile");

        if (keyGeneratorProfile == null)
            errors.add("'keyGeneratorProfile' not specified in section keyManagementProfile");

        if (keyRotationProfile == null)
            errors.add("'keyRotationProfile' not specified in section keyManagementProfile");

        if (!errors.isEmpty())
            return errors;

        // Recursive Validation
        errors.addAll(jwkStoreProfile.getErrors(context, rootConfig));
        errors.addAll(keyGeneratorProfile.getErrors(context, rootConfig));
        errors.addAll(keyRotationProfile.getErrors(context, rootConfig));

        return errors;
    }
}
