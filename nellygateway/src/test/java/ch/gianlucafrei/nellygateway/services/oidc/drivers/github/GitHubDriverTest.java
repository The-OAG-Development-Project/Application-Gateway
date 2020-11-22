package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubDriverTest {

  @Test
  public void getCallbackTest() throws ParseException {

      // Arrange
      var settings = GitHubDriverSettingsTest.getValidSettings();
      var driver = GitHubDriverSettingsTest.getDriver(settings);

      // Act
      LoginState loginState1 = driver.getRedirectUri();
      LoginState loginState2 = driver.getRedirectUri();

      // Assert

      // Check if the values from the oauth2 request are expected
      AuthorizationRequest req1 = AuthorizationRequest.parse(loginState1.getRedirectURI());
      assertEquals(req1.getState().toString(), loginState1.getState());
      assertEquals(req1.getClientID().toString(), settings.get("clientId"));
      assertTrue(req1.getScope().contains("email"));
      assertEquals(req1.getRedirectionURI(), driver.getCallbackUri());

      // Check if the login states are not the same
      assertNotEquals(loginState1.getState(), loginState2.getState(), "State variables must not be equal");
  }

}