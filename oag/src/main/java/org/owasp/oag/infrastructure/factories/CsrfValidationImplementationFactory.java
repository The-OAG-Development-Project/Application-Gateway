package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.context.ApplicationContext;

/**
 * Factory interface for creating CSRF validation implementations.
 * This factory is responsible for loading the appropriate CSRF protection validation
 * implementation based on the specified protection name.
 */
public interface CsrfValidationImplementationFactory {

    /**
     * Returns the CsrfProtection Implementation matching the Class {@code Csrf<csrfProtectionName>Validation}.
     * @param csrfProtectionName the name of the csrf implementation
     * @return the Class implementing the mechanism.
     */
    CsrfProtectionValidation loadCsrfValidationImplementation(String csrfProtectionName);

    /**
     * Retrieves the CsrfValidationImplementationFactory instance from the Spring application context.
     *
     * @param context The Spring application context
     * @return The CsrfValidationImplementationFactory instance
     */
    static CsrfValidationImplementationFactory get(ApplicationContext context){
        return context.getBean(CsrfValidationImplementationFactory.class);
    }
}
