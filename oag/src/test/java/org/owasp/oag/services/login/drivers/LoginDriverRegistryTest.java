package org.owasp.oag.services.login.drivers;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.github.GithubDriver;
import org.owasp.oag.services.login.drivers.github.GithubLoginDriverFactory;
import org.owasp.oag.services.login.drivers.oidc.OidcDriver;
import org.owasp.oag.services.login.drivers.oidc.OidcLoginDriverFactory;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginDriverRegistryTest {

    private final LoginDriverRegistry registry = new LoginDriverRegistry(
            List.of(new OidcLoginDriverFactory(), new GithubLoginDriverFactory()));

    @Test
    void registersBuiltInDriverTypes() {
        assertEquals(Set.of("oidc", "github"), registry.types());
    }

    @Test
    void loadCreatesDriverOfConfiguredType() {
        var settings = new LoginProviderSettings();
        settings.put("authEndpoint", "https://foo");
        settings.put("tokenEndpoint", "https://bar");
        settings.put("clientId", "foo");
        settings.put("clientSecret", "bar");
        settings.put("scopes", new String[]{"openid", "user", "email"});

        assertInstanceOf(OidcDriver.class, registry.load("oidc", settings));
        assertInstanceOf(GithubDriver.class, registry.load("github", settings));
    }
}
