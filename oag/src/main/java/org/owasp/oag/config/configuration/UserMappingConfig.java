package org.owasp.oag.config.configuration;

import org.owasp.oag.config.Subconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.owasp.oag.services.tokenMapping.UserMappingFactory.USER_MAPPER_TYPE_POSTFIX;

public class UserMappingConfig implements Subconfig {

    private static final Logger log = LoggerFactory.getLogger(UserMappingConfig.class);

    private String type;
    private Map<String, Object> settings;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    @Override
    public List<String> getErrors(ApplicationContext context, MainConfig rootConfig) {

        var errors = new ArrayList<String>();

        if (context == null)
            return errors;

        if (this.type == null)
            errors.add("Config: tokenMapping implementation is not defined");

        // Check if we can load the token mapping implementation
        if (!context.containsBean(this.type + USER_MAPPER_TYPE_POSTFIX)) {
            errors.add("Specified type '" + this.type + "' does not match a user mapping implementation. Must be the bean name of a TokenMapper implementation such as jwt-mapping.");
        } else {
            log.debug("Using token mapping implementation of {}.", this.type);
        }

        return errors;
    }
}
