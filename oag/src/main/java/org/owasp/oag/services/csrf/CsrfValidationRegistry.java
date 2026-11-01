package org.owasp.oag.services.csrf;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link CsrfProtectionValidation} implementations, keyed by {@code csrfProtection} value.
 */
@Component
public class CsrfValidationRegistry extends TypeRegistry<CsrfProtectionValidation> {

    /**
     * Creates the registry.
     *
     * @param validations All CSRF protection validations
     */
    public CsrfValidationRegistry(List<CsrfProtectionValidation> validations) {
        super("CSRF protection", validations);
    }
}
