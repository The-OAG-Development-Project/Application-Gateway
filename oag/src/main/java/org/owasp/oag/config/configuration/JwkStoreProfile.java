package org.owasp.oag.config.configuration;

import org.owasp.oag.config.Subconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the configured jwkStoreProfile section in the config file
 */
public class JwkStoreProfile implements Subconfig {

    private static final Logger log = LoggerFactory.getLogger(JwkStoreProfile.class);

    private String type;
    private Map<String, Object> implSpecificSettings = new HashMap<>();

    public JwkStoreProfile() {
    }

    public JwkStoreProfile(String type, Map<String, Object> implSpecificSettings) {
        this.type = type;
        this.implSpecificSettings = implSpecificSettings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getImplSpecificSettings() {
        return implSpecificSettings;
    }

    public void setTraceImplSpecificSettings(Map<String, Object> implSpecificSettings) {
        this.implSpecificSettings = implSpecificSettings;
    }

    @Override
    public List<String> getErrors(ApplicationContext context, MainConfig rootConfig) {

        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("'type' not specified. Must be the bean name of a JwkStore implementation such as localRsaJwkStore.");

        if (context != null && type != null && !context.containsBean(type)) {
            errors.add("Specified type '" + type + "' does not match a JwkStore implementation. Must be the bean name of a JwkStore implementation such as localRsaJwkStore.");
        } else {
            log.info("Using JwkStore implementation of {}.", type);
        }

        return errors;
    }
}
