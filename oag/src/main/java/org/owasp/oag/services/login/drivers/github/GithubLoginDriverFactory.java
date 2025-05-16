package org.owasp.oag.services.login.drivers.github;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriverFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating GitHub login driver instances.
 * This component is responsible for instantiating GitHub-specific
 * login drivers with the appropriate settings.
 */
@Component
public class GithubLoginDriverFactory implements LoginDriverFactory<GithubDriver> {

    /**
     * Creates a new GitHub login driver with the provided settings.
     *
     * @param settings The configuration settings for the GitHub login provider
     * @return A new GithubDriver instance configured with the provided settings
     */
    @Override
    public GithubDriver load(LoginProviderSettings settings) {
        return new GithubDriver(settings);
    }
}
