package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link LoginDriverFactory} implementations, keyed by login provider type.
 */
@Component
public class LoginDriverRegistry extends TypeRegistry<LoginDriverFactory> {

    /**
     * Creates the registry.
     *
     * @param factories All login driver factories
     */
    public LoginDriverRegistry(List<LoginDriverFactory> factories) {
        super("login driver", factories);
    }

    /**
     * Creates the login driver for the given login provider type.
     *
     * @param type     The login provider type
     * @param settings The settings of the login provider
     * @return A configured login driver
     * @throws org.owasp.oag.exception.ConfigurationException If the type is unknown
     * @throws InvalidProviderSettingsException                If the settings are invalid
     */
    public LoginDriver load(String type, LoginProviderSettings settings) {
        return get(type).load(settings);
    }
}
