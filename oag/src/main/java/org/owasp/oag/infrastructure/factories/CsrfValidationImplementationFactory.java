package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.context.ApplicationContext;

/**
 * Factory to create/get the configured CsrfValidationFactory required.
 */
public interface CsrfValidationImplementationFactory {

    // TODO: Gian-Luca please comment and document
    CsrfProtectionValidation loadCsrfValidationImplementation(String csrfProtectionName);

    static CsrfValidationImplementationFactory get(ApplicationContext context){
        return context.getBean(CsrfValidationImplementationFactory.class);
    }
}
