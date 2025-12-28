package org.owasp.oag.services.login.drivers.oidc;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriverFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating OpenID Connect (OIDC) login driver instances.
 * This component is responsible for instantiating OIDC-specific
 * login drivers with the appropriate settings.
 */
@Component
public class OidcLoginDriverFactory implements LoginDriverFactory<OidcDriver> {

    /**
     * Creates a new OIDC login driver with the provided settings.
     *
     * @param settings The configuration settings for the OIDC login provider
     * @return A new OidcDriver instance configured with the provided settings
     */
    @Override
    public OidcDriver load(LoginProviderSettings settings) {
        return new OidcDriver(settings);
    }
}
