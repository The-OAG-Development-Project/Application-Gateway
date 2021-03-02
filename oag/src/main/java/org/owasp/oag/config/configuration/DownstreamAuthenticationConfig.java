package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownstreamAuthenticationConfig implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(DownstreamAuthenticationConfig.class);
    private TokenMappingConfig tokenMapping;

    public DownstreamAuthenticationConfig() {
    }

    public DownstreamAuthenticationConfig(TokenMappingConfig tokenMapping) {
        this.tokenMapping = tokenMapping;
    }

    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if(tokenMapping == null)
            errors.add("Config: tokenMapping is not defined");

        if(errors.size() > 0)
            return errors;

        if(context == null)
            return errors;

        if(tokenMapping.implementation == null)
            errors.add("Config: tokenMapping implementation is not defined");

        // Check if we can load the token mapping implementation
        if (!context.containsBean(tokenMapping.implementation)) {
            errors.add("Specified type '" + tokenMapping.implementation + "' does not match a token mapping implementation. Must be the bean name of a TokenMapper implementation such as jwt-mapping.");
        } else {
            log.info("Using token mapping implementation of {}.", tokenMapping.implementation);
        }

        return errors;
    }

    public TokenMappingConfig getTokenMapping() {
        return tokenMapping;
    }

    public void setTokenMapping(TokenMappingConfig tokenMapping) {
        this.tokenMapping = tokenMapping;
    }


    public static class TokenMappingConfig {

        private String implementation;
        private Map<String, Object> settings;

        public String getImplementation() {
            return implementation;
        }

        public void setImplementation(String implementation) {
            this.implementation = implementation;
        }

        public Map<String, Object> getSettings() {
            return settings;
        }

        public void setSettings(Map<String, Object> settings) {
            this.settings = settings;
        }
    }
}
