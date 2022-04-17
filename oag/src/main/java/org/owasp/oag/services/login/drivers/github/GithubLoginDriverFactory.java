package org.owasp.oag.services.login.drivers.github;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriverFactory;
import org.springframework.stereotype.Component;

@Component
public class GithubLoginDriverFactory implements LoginDriverFactory<GithubDriver> {

    @Override
    public GithubDriver load(LoginProviderSettings settings) {
        return new GithubDriver(settings);
    }
}
