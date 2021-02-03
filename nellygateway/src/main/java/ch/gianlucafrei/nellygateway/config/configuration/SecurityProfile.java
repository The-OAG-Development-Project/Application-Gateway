package ch.gianlucafrei.nellygateway.config.configuration;

import ch.gianlucafrei.nellygateway.config.ErrorValidation;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfProtectionValidation;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityProfile implements ErrorValidation {

    private List<String> allowedMethods;
    private String csrfProtection;
    private List<String> csrfSafeMethods = Lists.asList("GET", new String[]{"HEAD", "OPTIONS"});
    private Map<String, String> responseHeaders = new HashMap<>();


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

    private void setResponseHeaders(Map<String, String> headers) {

        if (headers == null)
            this.responseHeaders = new HashMap<>();
        else
            this.responseHeaders = headers;
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
            errors.add("'allowedMethods' not specified");

        if (csrfProtection == null)
            errors.add("'csrfProtection' not specified");

        if (csrfSafeMethods == null)
            errors.add("'csrfSafeMethods' not specified");

        if (responseHeaders == null)
            errors.add("'responseHeaders' not specified");

        try {
            CsrfProtectionValidation.loadValidationImplementation(csrfProtection, context);
        } catch (NoSuchBeanDefinitionException ex) {
            errors.add("No csrf implementation found for '" + csrfProtection + "'");
        }

        return errors;
    }
}
