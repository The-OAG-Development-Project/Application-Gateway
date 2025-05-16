package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.springframework.context.ApplicationContext;

/**
 * Factory interface for creating login driver instances.
 * This factory is responsible for instantiating the appropriate
 * {@link LoginDriver} implementation based on the driver name.
 */
public interface LoginDriverFactory {
    /**
     * Loads a login driver instance based on the specified driver name and settings.
     *
     * @param driverName The name/identifier of the login driver to load
     * @param settings   The configuration settings for the login provider
     * @return A configured {@link LoginDriver} instance
     */
    LoginDriver loadDriverByKey(String driverName, LoginProviderSettings settings);

    /**
     * Retrieves the LoginDriverFactory instance from the Spring application context.
     *
     * @param context The Spring application context
     * @return The LoginDriverFactory instance
     */
    static LoginDriverFactory get(ApplicationContext context){
        return context.getBean(LoginDriverFactory.class);
    }
}
