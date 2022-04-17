package org.owasp.oag.services.login.drivers.github;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GithubDriverSettingsTest {


    @Test
    void getSettingsErrorsTestValidSettings() {

        // Arrange
        var settings = getValidSettings();

        // Act
        GithubDriver driver = getDriver(settings);

        // Assert
        assertNotNull(driver);
    }

    public static LoginProviderSettings getValidSettings() {

        LoginProviderSettings settings = new LoginProviderSettings();
        settings.put("authEndpoint", "https://github.com/login/oauth/authorize");
        settings.put("tokenEndpoint", "https://github.com/login/oauth/access_token");
        settings.put("clientId", "foo");
        settings.put("clientSecret", "bar");
        settings.put("scopes", new String[]{"user", "email", "test"});
        return settings;
    }

    public static GithubDriver getDriver(LoginProviderSettings settings) {

        return new GithubDriver(settings);
    }

    @Test
    void getSettingsErrorsTestNoClientId() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("clientId");

        // Act
        assertThrows(ConfigurationException.class, () -> getDriver(settings));
    }

    @Test
    void getSettingsErrorsTestNoClientSecret() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("clientSecret");

        // Act
        assertThrows(ConfigurationException.class, () -> getDriver(settings));
    }

    @Test
    void getSettingsErrorsTestNoAuthEndpoint() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("authEndpoint");

        // Act
        assertThrows(ConfigurationException.class, () -> getDriver(settings));
    }

    @Test
    void getSettingsErrorsTestNoTokenEndpoint() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("tokenEndpoint");

        // Act
        assertThrows(ConfigurationException.class, () -> getDriver(settings));
    }

    @Test
    void getSettingsErrorsTestInvalidScope() {

        // Arrange
        var settings = getValidSettings();
        settings.put("scopes", new String[]{"user"}); // no email in scope

        // Act
        assertThrows(ConfigurationException.class, () -> getDriver(settings));
    }

}