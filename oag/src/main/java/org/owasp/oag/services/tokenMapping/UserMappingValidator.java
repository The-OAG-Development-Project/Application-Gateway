package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that the {@code userMapping.type} of every security profile is a registered
 * {@link UserMappingFactory}.
 */
@Component
public class UserMappingValidator implements ConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(UserMappingValidator.class);

    private final UserMappingRegistry registry;

    /**
     * Creates the validator.
     *
     * @param registry The user mapping registry
     */
    public UserMappingValidator(UserMappingRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<String> validate(MainConfig config) {
        var errors = new ArrayList<String>();
        config.getSecurityProfiles().forEach((name, profile) -> {
            var type = profile.getUserMapping().getType();
            registry.unknownTypeError("UserMapping of security profile '" + name + "'", type).ifPresentOrElse(errors::add,
                    () -> log.debug("Using token mapping implementation of {}.", type));
        });
        return errors;
    }
}
