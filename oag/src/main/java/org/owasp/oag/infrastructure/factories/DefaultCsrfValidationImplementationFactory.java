package org.owasp.oag.infrastructure.factories;

import org.apache.commons.lang3.StringUtils;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the CsrfValidationImplementationFactory interface.
 * This factory is responsible for loading CSRF validation implementations by name
 * from the Spring application context.
 */
@Component
public class DefaultCsrfValidationImplementationFactory implements CsrfValidationImplementationFactory{

    /**
     * The Spring application context used to retrieve CSRF validation beans.
     */
    @Autowired
    ApplicationContext context;

    /**
     * Loads a CSRF validation implementation by name from the Spring application context.
     * The bean name is constructed by prefixing "csrf" and suffixing "Validation" to the capitalized protection name.
     *
     * @param csrfProtectionName The name of the CSRF protection mechanism to load
     * @return The CSRF protection validation implementation
     * @throws ConfigurationException If the specified CSRF mechanism cannot be found
     */
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
