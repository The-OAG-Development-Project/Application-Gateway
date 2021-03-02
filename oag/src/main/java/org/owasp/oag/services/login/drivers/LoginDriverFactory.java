package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;

public interface LoginDriverFactory<T extends LoginDriver> {

    T load(LoginProviderSettings settings);

}
