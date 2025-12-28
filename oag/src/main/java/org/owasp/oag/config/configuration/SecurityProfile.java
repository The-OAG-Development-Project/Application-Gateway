package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Defines a security profile that can be applied to gateway routes.
 * A security profile controls allowed HTTP methods, CSRF protection,
 * response headers, and user mapping configurations.
 */
public class SecurityProfile implements ErrorValidation {

    private List<String> allowedMethods;
    private String csrfProtection;
    private List<String> csrfSafeMethods = DefaultConfigValues.csrfSafeMethods();
    private Map<String, String> responseHeaders = DefaultConfigValues.responseHeaders();
    private UserMappingConfig userMapping = DefaultConfigValues.userMapping();

    /**
     * Gets the list of HTTP methods allowed for this security profile.
     * 
     * @return List of allowed HTTP methods
     */
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * Sets the list of HTTP methods allowed for this security profile.
     * 
     * @param allowedMethods List of allowed HTTP methods
     */
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * Gets the CSRF protection method used by this security profile.
     * 
     * @return The CSRF protection method
     */
    public String getCsrfProtection() {
        return csrfProtection;
    }

    /**
     * Sets the CSRF protection method used by this security profile.
     * 
     * @param csrfProtection The CSRF protection method
     */
    public void setCsrfProtection(String csrfProtection) {
        this.csrfProtection = csrfProtection;
    }

    /**
     * Gets the map of response headers that should be applied to responses.
     * 
     * @return Map of header names to header values
     */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Gets the user mapping configuration for this security profile.
     * 
     * @return The user mapping configuration
     */
    public UserMappingConfig getUserMapping() {
        return userMapping;
    }

    /**
     * Sets the user mapping configuration for this security profile.
     * 
     * @param userMapping The user mapping configuration
     */
    public void setUserMapping(UserMappingConfig userMapping) {
        this.userMapping = userMapping;
    }

    /**
     * Sets the response headers map for this security profile.
     * If null is provided, an empty map will be used.
     * 
     * @param headers Map of header names to header values
     */
    private void setResponseHeaders(Map<String, String> headers) {
        this.responseHeaders = Objects.requireNonNullElseGet(headers, HashMap::new);
    }

    /**
     * Gets the list of HTTP methods that are considered safe from CSRF attacks.
     * 
     * @return List of CSRF-safe HTTP methods
     */
    public List<String> getCsrfSafeMethods() {
        return this.csrfSafeMethods;
    }

    /**
     * Sets the list of HTTP methods that are considered safe from CSRF attacks.
     * 
     * @param csrfSafeMethods List of CSRF-safe HTTP methods
     */
    public void setCsrfSafeMethods(List<String> csrfSafeMethods) {
        this.csrfSafeMethods = csrfSafeMethods;
    }

    /**
     * Validates the security profile configuration and returns any errors found.
     * 
     * @param context The application context
     * @return A list of validation error messages, empty if no errors are found
     */
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
