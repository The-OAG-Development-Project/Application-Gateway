package ch.gianlucafrei.nellygateway.services.login.drivers.oidc;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriver;
import ch.gianlucafrei.nellygateway.services.login.drivers.github.GitHubDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class LoginDriverLoader {

    private final ApplicationContext context;

    public LoginDriverLoader(@Autowired ApplicationContext context) {
        this.context = context;
    }

    public LoginDriver loadDriverByKey(String driverName, URI callbackURI, LoginProviderSettings settings) {

        if ("oidc".equals(driverName))
            return new OidcDriver(settings, callbackURI);

        if ("github".equals(driverName))
            return new GitHubDriver(settings, callbackURI);

        throw new RuntimeException("Login driver with name " + driverName + " not found");
    }
}
