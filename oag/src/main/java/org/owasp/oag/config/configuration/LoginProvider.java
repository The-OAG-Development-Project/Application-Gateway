package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.infrastructure.factories.LoginDriverFactory;
import org.owasp.oag.services.login.drivers.InvalidProviderSettingsException;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for login providers.
 * This class represents the configuration for an authentication provider,
 * such as OIDC, GitHub, or other identity providers.
 */
public class LoginProvider implements ErrorValidation {

    /**
     * The type of login provider (e.g., "oidc", "github").
     * This corresponds to a login driver implementation.
     */
    private String type;
    
    /**
     * Provider-specific settings for this login provider.
     */
    private LoginProviderSettings with;

    /**
     * Default constructor.
     */
    public LoginProvider() {
    }

    /**
     * Creates a new login provider configuration with the specified type and settings.
     *
     * @param type The type of login provider
     * @param with The provider-specific settings
     */
    public LoginProvider(String type, LoginProviderSettings with) {
        this.type = type;
        this.with = with;
    }

    /**
     * Gets the type of login provider.
     *
     * @return The login provider type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of login provider.
     *
     * @param type The login provider type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the provider-specific settings.
     *
     * @return The provider settings
     */
    public LoginProviderSettings getWith() {
        return with;
    }

    /**
     * Sets the provider-specific settings.
     *
     * @param with The provider settings
     */
    public void setWith(LoginProviderSettings with) {
        this.with = with;
    }

    /**
     * Validates the login provider configuration and returns a list of validation errors.
     * This method attempts to load the appropriate login driver to validate the settings.
     *
     * @param context The Spring application context
     * @return A list of error messages, empty if the configuration is valid
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("LoginProvider: No type defined");
        if (with == null)
            errors.add("LoginProvider: No settings defined");

        if (!errors.isEmpty())
            return errors;

        // Check if we can load the driver
        LoginDriverFactory factory = LoginDriverFactory.get(context);
        try {
            LoginDriver loginDriver = factory.loadDriverByKey(type, with);
        } catch (InvalidProviderSettingsException e) {
            var settingErrors = e.getSettingErrors();
            errors.addAll(settingErrors);
        } catch (Exception e) {
            errors.add("LoginDriver: Could not load driver implementation for type '" + type + "'");
        }

        return errors;
    }
}
