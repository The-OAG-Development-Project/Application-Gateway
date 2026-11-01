package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.extension.TypedExtension;

/**
 * Creates the login driver of one login provider type.
 * The factory is selected through the login provider's {@code type} value, see {@link #type()}.
 */
public interface LoginDriverFactory extends TypedExtension {

    /**
     * Creates and configures a login driver instance based on the provided settings.
     *
     * @param settings The configuration settings for the login provider
     * @return A new login driver instance configured with the provided settings
     * @throws InvalidProviderSettingsException If the settings are invalid or incomplete
     */
    LoginDriver load(LoginProviderSettings settings);

}
