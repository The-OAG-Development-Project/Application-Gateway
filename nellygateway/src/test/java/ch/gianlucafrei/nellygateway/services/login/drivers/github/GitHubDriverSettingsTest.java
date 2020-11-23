package ch.gianlucafrei.nellygateway.services.login.drivers.github;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProviderSettings;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class GitHubDriverSettingsTest {


    @Test
    void getSettingsErrorsTestValidSettings() {

        // Arrange
        var settings = getValidSettings();

        // Act
        GitHubDriver driver = getDriver(settings);

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

    public static GitHubDriver getDriver(LoginProviderSettings settings) {

        try {
            return new GitHubDriver(settings, new URI("http://localhost:7777"));
        } catch (URISyntaxException e) {
            fail("Should not happen");
            return null;
        }
    }

    @Test
    void getSettingsErrorsTestNoClientId() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("clientId");

        // Act
        assertThrows(RuntimeException.class, () -> {
            getDriver(settings);
        });
    }

    @Test
    void getSettingsErrorsTestNoClientSecret() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("clientSecret");

        // Act
        assertThrows(RuntimeException.class, () -> {
            getDriver(settings);
        });
    }

    @Test
    void getSettingsErrorsTestNoAuthEndpoint() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("authEndpoint");

        // Act
        assertThrows(RuntimeException.class, () -> {
            getDriver(settings);
        });
    }

    @Test
    void getSettingsErrorsTestNoTokenEndpoint() {

        // Arrange
        var settings = getValidSettings();
        settings.remove("tokenEndpoint");

        // Act
        assertThrows(RuntimeException.class, () -> {
            getDriver(settings);
        });
    }

    @Test
    void getSettingsErrorsTestInvalidScope() {

        // Arrange
        var settings = getValidSettings();
        settings.put("scopes", new String[]{"user"}); // no email in scope

        // Act
        assertThrows(RuntimeException.class, () -> {
            getDriver(settings);
        });
    }

}