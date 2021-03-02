package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultCsrfValidationImplementationFactory implements CsrfValidationImplementationFactory{

    @Autowired
    ApplicationContext context;

    @Override
    public CsrfProtectionValidation loadCsrfValidationImplementation(String csrfProtectionName) {

        String beanname = "csrf-" + csrfProtectionName + "-validation";
        CsrfProtectionValidation validationImplementation = context.getBean(beanname, CsrfProtectionValidation.class);

        if (validationImplementation == null) {
            throw new RuntimeException("csrf validation implementation not found: " + beanname);
        }

        return validationImplementation;
    }
}
