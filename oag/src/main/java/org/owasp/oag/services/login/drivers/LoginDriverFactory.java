package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;

/**
 * Factory interface for creating specific login driver instances.
 * This factory is responsible for loading and configuring a specific type of login driver
 * based on the provided settings.
 *
 * @param <T> The specific type of LoginDriver this factory creates
 */
public interface LoginDriverFactory<T extends LoginDriver> {

    /**
     * Creates and configures a login driver instance based on the provided settings.
     *
     * @param settings The configuration settings for the login provider
     * @return A new login driver instance configured with the provided settings
     * @throws InvalidProviderSettingsException If the settings are invalid or incomplete
     */
    T load(LoginProviderSettings settings);

}
