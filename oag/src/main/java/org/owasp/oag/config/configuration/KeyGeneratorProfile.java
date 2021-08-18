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
 * Provides access to the configured keyGeneratorProfile section in the config file
 */
public class KeyGeneratorProfile implements Subconfig {

    private static final Logger log = LoggerFactory.getLogger(KeyGeneratorProfile.class);

    private String type;
    private Integer keySize;
    private Map<String, Object> implSpecificSettings = new HashMap<>();

    public KeyGeneratorProfile() {
    }

    public KeyGeneratorProfile(String type, Integer keySize, Map<String, Object> implSpecificSettings) {
        this.type = type;
        this.keySize = keySize;
        this.implSpecificSettings = implSpecificSettings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
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
            errors.add("'type' not specified. Must be the bean name of a KeyGenerator implementation such as rsaKeyGenerator.");

        if (keySize == null || keySize < 64)
            errors.add("'keySize' not specified or way to short.");

        if (context != null && type != null && !context.containsBean(type)) {
            errors.add("Specified type '" + type + "' does not match a KeyGenerator implementation. Must be the bean name of a KeyGenerator implementation such as rsaKeyGenerator.");
        } else {
            log.info("Using KeyGenerator implementation of {}.", type);
        }

        return errors;
    }
}
