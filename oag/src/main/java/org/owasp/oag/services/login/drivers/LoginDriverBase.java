package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;

public abstract class LoginDriverBase implements LoginDriver {


    private final LoginProviderSettings settings;

    public LoginDriverBase(LoginProviderSettings settings) {

        var errors = getSettingsErrors(settings);
        if (errors.isEmpty()) {
            this.settings = settings;
        } else {
            throw new InvalidProviderSettingsException(errors);
        }
    }

    public LoginProviderSettings getSettings() {
        return settings;
    }
}
