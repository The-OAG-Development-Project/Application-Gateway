package org.owasp.oag.services.login.drivers.github;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriverFactory;
import org.springframework.stereotype.Component;

@Component("github-driver-factory")
public class GitHubDriverFactory implements LoginDriverFactory<GitHubDriver> {

    @Override
    public GitHubDriver load(LoginProviderSettings settings) {
        return new GitHubDriver(settings);
    }
}
