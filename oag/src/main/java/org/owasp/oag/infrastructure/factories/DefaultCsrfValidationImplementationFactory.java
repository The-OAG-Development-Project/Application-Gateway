package org.owasp.oag.infrastructure.factories;

import org.apache.commons.lang3.StringUtils;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.beans.BeansException;
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
        try {
            return context.getBean(beanName, CsrfProtectionValidation.class);
        } catch (BeansException be){
            throw new ConfigurationException("Could not find CSRF Mechanism with name: "+csrfProtectionName+". It is expected to be a Bean named: "+beanName);
        }
    }
}
