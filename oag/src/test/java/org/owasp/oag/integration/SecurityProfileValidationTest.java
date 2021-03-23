package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.integration.testInfrastructure.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityProfileValidationTest extends IntegrationTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void testGetErrorsValid() {

        // Arrange
        SecurityProfile profile = new SecurityProfile();
        profile.setAllowedMethods(new ArrayList<>());
        profile.setCsrfSafeMethods(new ArrayList<>());

        // Act
        profile.setCsrfProtection("double-submit-cookie");
        List<String> errors1 = profile.getErrors(context);
        profile.setCsrfProtection("samesite-strict-cookie");
        List<String> errors2 = profile.getErrors(context);
        profile.setCsrfProtection("none");
        List<String> errors3 = profile.getErrors(context);

        // Assert
        assertEquals(0, errors1.size());
    }

    @Test
    public void testGetErrorsInvalidCsrfProtection() {

        // Arrange
        SecurityProfile profile = new SecurityProfile();
        profile.setAllowedMethods(new ArrayList<>());
        profile.setCsrfSafeMethods(new ArrayList<>());

        // Act
        profile.setCsrfProtection("does-not-exist");
        List<String> errors = profile.getErrors(context);

        // Assert
        assertEquals(1, errors.size());
    }


    @Test
    public void testGetErrorsNoSettings() {

        // Arrange
        SecurityProfile profile = new SecurityProfile();

        // Act
        List<String> errors = profile.getErrors(context);

        // Assert
        assertEquals(2, errors.size());
    }

}