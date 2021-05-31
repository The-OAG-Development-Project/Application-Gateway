package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the configured KeyRotationProfile in Section KeyManagementProfile of the configuration.
 */
public class KeyRotationProfile implements ErrorValidation {
    private static final Logger log = LoggerFactory.getLogger(KeyRotationProfile.class);

    private String type;
    private Boolean useSigningKeyRotation;
    private Integer signingKeyRotationSeconds;
    private Integer cleanupFrequencySeconds;

    public KeyRotationProfile() {
    }

    public KeyRotationProfile(String type, Boolean useSigningKeyRotation, Integer signingKeyRotationSeconds, Integer cleanupFrequencySeconds) {
        this.type = type;
        this.useSigningKeyRotation = useSigningKeyRotation;
        this.signingKeyRotationSeconds = signingKeyRotationSeconds;
        this.cleanupFrequencySeconds = cleanupFrequencySeconds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getUseSigningKeyRotation() {
        return useSigningKeyRotation;
    }

    public void setUseSigningKeyRotation(Boolean useSigningKeyRotation) {
        this.useSigningKeyRotation = useSigningKeyRotation;
    }

    public Integer getSigningKeyRotationSeconds() {
        return signingKeyRotationSeconds;
    }

    public void setSigningKeyRotationSeconds(Integer signingKeyRotationSeconds) {
        this.signingKeyRotationSeconds = signingKeyRotationSeconds;
    }

    public Integer getCleanupFrequencySeconds() {
        return cleanupFrequencySeconds;
    }

    public void setCleanupFrequencySeconds(Integer cleanupFrequencySeconds) {
        this.cleanupFrequencySeconds = cleanupFrequencySeconds;
    }

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
