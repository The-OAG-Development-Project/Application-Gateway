package org.owasp.oag.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true", "logging.level.org.owasp.oag=TRACE"})
@AutoConfigureWebTestClient
class LoginProvidorValidationTest {

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
        assertEquals(0, errors.size(), "Errors: " + errors);
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
        assertFalse(errors.isEmpty(), "Expected errors with invalid configuration");
    }
}