package org.owasp.oag.config;

import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates the whole configuration: first its structure, then, if the structure is valid, all
 * {@link ConfigValidator}s of the extension points.
 */
@Component
public class MainConfigValidator {

    private final List<ConfigValidator> validators;

    /**
     * Creates the validator.
     *
     * @param validators The validators of all extension points
     */
    public MainConfigValidator(List<ConfigValidator> validators) {
        this.validators = validators;
    }

    /**
     * Validates the configuration.
     *
     * @param config The configuration to validate
     * @return A list of error messages, or an empty list if the configuration is valid
     */
    public List<String> validate(MainConfig config) {
        var errors = config.getErrors();
        if (!errors.isEmpty())
            return errors;
        return validators.stream().flatMap(validator -> validator.validate(config).stream()).toList();
    }
}
