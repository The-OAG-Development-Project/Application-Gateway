package org.owasp.oag.infrastructure.factories;

import org.apache.commons.lang3.StringUtils;
import org.owasp.oag.exception.ConfigurationException;
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

        String beanName = "csrf" + StringUtils.capitalize(csrfProtectionName) + "Validation";
        CsrfProtectionValidation validationImplementation = context.getBean(beanName, CsrfProtectionValidation.class);

        if (validationImplementation == null) { // <-- TODO should not be necessary, due to .getBean throwing NoSuchBeanExceptions
            throw new ConfigurationException("csrf validation implementation not found: " + beanName, null);
        }

        return validationImplementation;
    }
}
