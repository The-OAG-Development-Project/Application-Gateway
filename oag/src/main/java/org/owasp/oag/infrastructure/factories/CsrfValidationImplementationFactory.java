package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.context.ApplicationContext;

/**
 * Factory to create/get the configured CsrfValidationFactory required.
 */
public interface CsrfValidationImplementationFactory {

    /**
     * Returns the CsrfProtection Implementation matching the Class Csrf<csrfProtectionName>Validation.
     * @param csrfProtectionName the name of the csrf implementation
     * @return the Class implementing the mechanism.
     */
    CsrfProtectionValidation loadCsrfValidationImplementation(String csrfProtectionName);

    static CsrfValidationImplementationFactory get(ApplicationContext context){
        return context.getBean(CsrfValidationImplementationFactory.class);
    }
}
