package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the configured KeyManagement section in the config file
 */
public class KeyManagementProfile implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(KeyManagementProfile.class);

    private JwkStoreProfile jwkStoreProfile;
    private KeyGeneratorProfile keyGeneratorProfile;
    private Boolean useSigningKeyRotation;
    private Integer signingKeyRotationHours;

    public KeyManagementProfile() {
    }

    public KeyManagementProfile(JwkStoreProfile jwkStoreProfile, KeyGeneratorProfile keyGeneratorProfile, Boolean useSigningKeyRotation,
                                Integer signingKeyRotationHours) {
        this.jwkStoreProfile = jwkStoreProfile;
        this.keyGeneratorProfile = keyGeneratorProfile;
        this.useSigningKeyRotation = useSigningKeyRotation;
        this.signingKeyRotationHours = signingKeyRotationHours;
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

    public Boolean getUseSigningKeyRotation() {
        return useSigningKeyRotation;
    }

    public void setUseSigningKeyRotation(Boolean useSigningKeyRotation) {
        this.useSigningKeyRotation = useSigningKeyRotation;
    }

    public Integer getSigningKeyRotationHours() {
        return signingKeyRotationHours;
    }

    public void setSigningKeyRotationHours(Integer signingKeyRotationHours) {
        this.signingKeyRotationHours = signingKeyRotationHours;
    }

    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (jwkStoreProfile == null)
            errors.add("'jwkStoreProfile' not specified in section keyManagementProfile");

        if (keyGeneratorProfile == null)
            errors.add("'keyGeneratorProfile' not specified in section keyManagementProfile");

        if (useSigningKeyRotation == null)
            errors.add("'useSigningKeyRotation' not specified in section keyManagementProfile");
        else {
            if (useSigningKeyRotation && (signingKeyRotationHours == null || signingKeyRotationHours < 1))
                errors.add("'signingKeyRotationHours' not specified in section keyManagementProfile or value <1 (must be 1 or more).");
        }

        if (!errors.isEmpty())
            return errors;

        errors.addAll(jwkStoreProfile.getErrors(context));
        errors.addAll(keyGeneratorProfile.getErrors(context));

        return errors;
    }
}
