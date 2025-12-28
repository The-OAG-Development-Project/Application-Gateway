package org.owasp.oag.config;

import org.owasp.oag.config.configuration.MainConfig;

import java.io.IOException;

/**
 * Interface for loading the main configuration.
 * Implementations of this interface provide different strategies for loading
 * the application configuration, such as from a file, a remote URL, or other sources.
 */
public interface ConfigLoader {
    /**
     * Loads the main configuration.
     * This method retrieves the configuration from its source and processes it
     * into a usable MainConfig object. It may also perform validation and merging
     * with default configurations.
     *
     * @return The loaded main configuration.
     * @throws IOException If an I/O error occurs while loading the configuration.
     */
    MainConfig loadConfiguration() throws IOException;
}
