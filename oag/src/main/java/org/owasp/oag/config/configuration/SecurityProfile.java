package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.*;

public class SecurityProfile implements ErrorValidation {

    private List<String> allowedMethods;
    private String csrfProtection;
    private List<String> csrfSafeMethods = DefaultConfigValues.csrfSafeMethods();
    private Map<String, String> responseHeaders = DefaultConfigValues.responseHeaders();
    private UserMappingConfig userMapping = DefaultConfigValues.userMapping();

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public String getCsrfProtection() {
        return csrfProtection;
    }

    public void setCsrfProtection(String csrfProtection) {
        this.csrfProtection = csrfProtection;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public UserMappingConfig getUserMapping() {
        return userMapping;
    }

    public void setUserMapping(UserMappingConfig userMapping) {
        this.userMapping = userMapping;
    }

    private void setResponseHeaders(Map<String, String> headers) {
        this.responseHeaders = Objects.requireNonNullElseGet(headers, HashMap::new);
    }

    public List<String> getCsrfSafeMethods() {

        return this.csrfSafeMethods;
    }

    public void setCsrfSafeMethods(List<String> csrfSafeMethods) {

        this.csrfSafeMethods = csrfSafeMethods;
    }

    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (allowedMethods == null)
            errors.add("Config Security Profile: 'allowedMethods' not specified");

        if (csrfProtection == null)
            errors.add("Config Security Profile: 'csrfProtection' not specified");

        if (csrfSafeMethods == null)
            errors.add("Config Security Profile: 'csrfSafeMethods' not specified");

        if (responseHeaders == null)
            errors.add("Config Security Profile: 'responseHeaders' not specified");

        if (userMapping == null)
            errors.add("Config Security Profile: 'userMapping' not specified");

        if (errors.size() > 0)
            return errors;

        errors.addAll(userMapping.getErrors(context));

        if (errors.size() > 0 || context == null)
            return errors;

        var factory = CsrfValidationImplementationFactory.get(context);
        try {
            CsrfProtectionValidation implementation = factory.loadCsrfValidationImplementation(csrfProtection);
        } catch (NoSuchBeanDefinitionException ex) {
            errors.add("CsrfProtection: No csrf implementation found for '" + csrfProtection + "'");
        }

        return errors;
    }
}
