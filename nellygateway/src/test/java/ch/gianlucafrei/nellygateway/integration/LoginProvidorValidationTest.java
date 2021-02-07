package ch.gianlucafrei.nellygateway.integration;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProvider;
import ch.gianlucafrei.nellygateway.config.configuration.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.integration.testInfrastructure.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginProvidorValidationTest extends IntegrationTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void testValidConfiguration() {

        // Arrange
        var settings = new LoginProviderSettings();
        settings.put("authEndpoint", "https://foo");
        settings.put("tokenEndpoint", "https://bar");
        settings.put("clientId", "foo");
        settings.put("clientSecret", "bar");
        settings.put("scopes", new String[]{"openid", "bar"});
        LoginProvider provider = new LoginProvider("oidc", settings);

        // Act
        var errors = provider.getErrors(context);

        // Assert
        assertEquals(0, errors.size(), "Errors: " + errors.toString());
    }

    @Test
    public void testNoFields() {

        // Arrange
        LoginProvider provider = new LoginProvider(null, null);

        // Act
        var errors = provider.getErrors(context);

        // Assert
        assertEquals(2, errors.size());
    }

    @Test
    public void testInvalidProviderName() {

        // Arrange
        LoginProvider provider = new LoginProvider("doesnotexist", new LoginProviderSettings());

        // Act
        var errors = provider.getErrors(context);

        // Assert
        assertEquals(1, errors.size());
    }

    @Test
    public void testInvalidInvalidSettings() {

        // Arrange
        LoginProvider provider = new LoginProvider("oidc", new LoginProviderSettings());

        // Act
        var errors = provider.getErrors(context);

        // Assert
        assertTrue(!errors.isEmpty(), "Expected errors with invalid configuration");
    }
}