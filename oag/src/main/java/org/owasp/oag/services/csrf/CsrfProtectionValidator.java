package org.owasp.oag.services.csrf;

import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that the {@code csrfProtection} of every security profile is a registered
 * {@link CsrfProtectionValidation}.
 */
@Component
public class CsrfProtectionValidator implements ConfigValidator {

    private final CsrfValidationRegistry registry;

    /**
     * Creates the validator.
     *
     * @param registry The CSRF validation registry
     */
    public CsrfProtectionValidator(CsrfValidationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<String> validate(MainConfig config) {
        return config.getSecurityProfiles().entrySet().stream()
                .flatMap(profile -> registry.unknownTypeError("CsrfProtection of security profile '" + profile.getKey() + "'",
                        profile.getValue().getCsrfProtection()).stream())
                .toList();
    }
}
