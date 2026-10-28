package org.owasp.oag.config.configuration;

import java.util.List;

/**
 * Validates the part of the configuration that depends on registered extensions, for example whether a
 * configured {@code type} is known. Each extension point provides one implementation as a Spring bean.
 */
public interface ConfigValidator {

    /**
     * Validates the configuration.
     *
     * @param config A configuration without structural errors (see {@link MainConfig#getErrors})
     * @return A list of error messages, or an empty list if no errors are found
     */
    List<String> validate(MainConfig config);
}
