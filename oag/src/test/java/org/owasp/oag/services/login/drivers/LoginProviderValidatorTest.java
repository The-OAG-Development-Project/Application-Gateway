package org.owasp.oag.services.login.drivers;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.services.login.drivers.oidc.OidcLoginDriverFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.owasp.oag.extension.TypedExtensionStub.stub;

class LoginProviderValidatorTest {

    private final LoginProviderValidator validator = new LoginProviderValidator(new LoginDriverRegistry(
            List.of(new OidcLoginDriverFactory(), stub(LoginDriverFactory.class, "broken"))));

    private static MainConfig configWithProvider(String type, LoginProviderSettings settings) {
        return new MainConfig(new HashMap<>(Map.of("idp", new LoginProvider(type, settings))), Map.of(), Map.of(),
                null, null, null, null, null);
    }

    private static LoginProviderSettings validOidcSettings() {
        var settings = new LoginProviderSettings();
        settings.put("authEndpoint", "https://foo");
        settings.put("tokenEndpoint", "https://bar");
        settings.put("clientId", "foo");
        settings.put("clientSecret", "bar");
        settings.put("scopes", new String[]{"openid", "bar"});
        return settings;
    }

    @Test
    void validProviderHasNoErrors() {
        assertEquals(List.of(), validator.validate(configWithProvider("oidc", validOidcSettings())));
    }

    @Test
    void unknownTypeIsReportedWithProviderName() {
        assertEquals(List.of("LoginProvider 'idp': Unknown type 'x'. Known types: [broken, oidc]"),
                validator.validate(configWithProvider("x", new LoginProviderSettings())));
    }

    @Test
    void invalidSettingsAreReported() {
        assertFalse(validator.validate(configWithProvider("oidc", new LoginProviderSettings())).isEmpty());
    }

    @Test
    void failingDriverCreationIsReported() {
        assertEquals(List.of("LoginProvider 'idp': Could not create driver of type 'broken': load"),
                validator.validate(configWithProvider("broken", new LoginProviderSettings())));
    }
}
