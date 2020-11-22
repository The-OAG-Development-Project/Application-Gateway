package ch.gianlucafrei.nellygateway.services.login.drivers.github;

import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriverResult;
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
      LoginDriverResult loginDriverResult1 = driver.startLogin();
      LoginDriverResult loginDriverResult2 = driver.startLogin();

      // Assert

      // Check if the values from the oauth2 request are expected
      AuthorizationRequest req1 = AuthorizationRequest.parse(loginDriverResult1.getAuthURI());
      assertEquals(req1.getState().toString(), loginDriverResult1.getState());
      assertEquals(req1.getClientID().toString(), settings.get("clientId"));
      assertTrue(req1.getScope().contains("email"));
      assertEquals(req1.getRedirectionURI(), driver.getCallbackUri());

      // Check if the login states are not the same
      assertNotEquals(loginDriverResult1.getState(), loginDriverResult2.getState(), "State variables must not be equal");
  }

}