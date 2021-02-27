package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.owasp.oag.services.login.drivers.github.GitHubDriver;
import org.owasp.oag.services.login.drivers.oidc.OidcDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoginDriverFactory implements LoginDriverFactory {

    private final ApplicationContext context;

    public DefaultLoginDriverFactory(@Autowired ApplicationContext context) {
        this.context = context;
    }

    @Override
    public LoginDriver loadDriverByKey(String driverName, LoginProviderSettings settings) {

        if ("oidc".equals(driverName))
            return new OidcDriver(settings);

        if ("github".equals(driverName))
            return new GitHubDriver(settings);

        throw new RuntimeException("Login driver with name " + driverName + " not found");
    }
}
