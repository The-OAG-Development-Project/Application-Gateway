package org.owasp.oag.services.login.drivers.oidc;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriverFactory;
import org.springframework.stereotype.Component;

@Component
public class OidcLoginDriverFactory implements LoginDriverFactory<OidcDriver> {

    @Override
    public OidcDriver load(LoginProviderSettings settings) {
        return new OidcDriver(settings);
    }
}
