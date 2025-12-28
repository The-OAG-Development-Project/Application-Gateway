package org.owasp.oag.config;

import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Interface for components that can validate their configuration and report errors.
 * Implementing classes should provide validation logic to check for configuration errors.
 */
public interface ErrorValidation {

    /**
     * Gets a list of validation errors for the current configuration.
     * 
     * @param context The application context, which may be used to validate against other components
     * @return A list of error messages, or an empty list if no errors are found
     */
    List<String> getErrors(ApplicationContext context);
}
