package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;

import java.net.URI;

public abstract class LoginDriverBase implements LoginDriver {


    private final LoginProviderSettings settings;
    private final URI callbackURI;

    public LoginDriverBase(LoginProviderSettings settings, URI callbackURI) {
        this.callbackURI = callbackURI;

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

    public URI getCallbackUri() {

        return callbackURI;
    }
}
