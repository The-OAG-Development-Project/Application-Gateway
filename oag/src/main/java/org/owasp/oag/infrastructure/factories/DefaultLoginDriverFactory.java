package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the LoginDriverFactory interface.
 * This factory is responsible for loading login driver instances based on their name
 * and configuration settings.
 */
@Component
public class DefaultLoginDriverFactory implements LoginDriverFactory {

    /**
     * The Spring application context used to retrieve login driver factories.
     */
    private final ApplicationContext context;

    /**
     * Constructs a new DefaultLoginDriverFactory with the specified application context.
     * 
     * @param context The Spring application context used to retrieve login driver factories
     */
    public DefaultLoginDriverFactory(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Loads a login driver instance based on the specified driver name and settings.
     * This method looks up the appropriate login driver factory bean in the application context
     * and uses it to create a login driver instance.
     *
     * @param driverName The name of the login driver to load
     * @param settings The settings to configure the login driver with
     * @return A configured login driver instance
     * @throws ConfigurationException If the login driver factory cannot be found or the driver cannot be loaded
     */
    @Override
    public LoginDriver loadDriverByKey(String driverName, LoginProviderSettings settings) {

        try {
            var driverFactory = context.getBean(driverName + LoginDriverFactory.class.getSimpleName(),
                    org.owasp.oag.services.login.drivers.LoginDriverFactory.class);

            return driverFactory.load(settings);
        } catch (Exception ex) {
            throw new ConfigurationException("Login driver factory with name " + driverName + " not found", ex);
        }
    }
}
