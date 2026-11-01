package org.owasp.oag.config.configuration;

import java.util.List;

/**
 * Implemented by configuration classes that validate their own structure, for example missing fields or values
 * out of range. Checks against registered extensions are done by {@link ConfigValidator}s.
 */
public interface ErrorValidation {

    /**
     * Gets a list of structural validation errors of this configuration.
     *
     * @return A list of error messages, or an empty list if no errors are found
     */
    List<String> getErrors();
}
