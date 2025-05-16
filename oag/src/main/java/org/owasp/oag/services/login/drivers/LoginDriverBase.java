package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;

/**
 * Base abstract class for login driver implementations.
 * This class provides common functionality for all login drivers,
 * including settings validation and management.
 */
public abstract class LoginDriverBase implements LoginDriver {

    /**
     * The settings for this login driver.
     */
    private final LoginProviderSettings settings;

    /**
     * Constructs a new login driver with the specified settings.
     * Validates the settings before initializing the driver.
     *
     * @param settings The login provider settings
     * @throws InvalidProviderSettingsException if the settings are invalid
     */
    public LoginDriverBase(LoginProviderSettings settings) {
        var errors = getSettingsErrors(settings);
        if (errors.isEmpty()) {
            this.settings = settings;
        } else {
            throw new InvalidProviderSettingsException(errors);
        }
    }

    /**
     * Gets the login provider settings for this driver.
     *
     * @return The login provider settings
     */
    public LoginProviderSettings getSettings() {
        return settings;
    }
}
