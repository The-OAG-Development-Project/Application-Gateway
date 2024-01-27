package org.owasp.oag.services.login.drivers.github;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ParseException;
import org.junit.jupiter.api.Test;
import org.owasp.oag.services.login.drivers.LoginDriverResult;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class GithubDriverTest {

    @Test
    public void getCallbackTest() throws ParseException {

        // Arrange
        var settings = GithubDriverSettingsTest.getValidSettings();
        var driver = GithubDriverSettingsTest.getDriver(settings);
        var callbackUri = URI.create("https://example/callback");

        // Act
        LoginDriverResult loginDriverResult1 = driver.startLogin(callbackUri);
        LoginDriverResult loginDriverResult2 = driver.startLogin(callbackUri);

        // Assert

        // Check if the values from the oauth2 request are expected
        AuthorizationRequest req1 = AuthorizationRequest.parse(loginDriverResult1.getAuthURI());
        assertEquals(req1.getState().toString(), loginDriverResult1.getState());
        assertEquals(req1.getClientID().toString(), settings.get("clientId"));
        assertTrue(req1.getScope().contains("email"));
        assertEquals(req1.getRedirectionURI(), callbackUri);

        // Check if the login states are not the same
        assertNotEquals(loginDriverResult1.getState(), loginDriverResult2.getState(), "State variables must not be equal");
    }

}