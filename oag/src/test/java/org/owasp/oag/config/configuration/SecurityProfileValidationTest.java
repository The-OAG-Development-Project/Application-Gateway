package org.owasp.oag.config.configuration;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityProfileValidationTest {

    @Test
    public void testGetErrorsValid() {

        // Arrange
        SecurityProfile profile = new SecurityProfile();
        profile.setAllowedMethods(new ArrayList<>());
        profile.setCsrfSafeMethods(new ArrayList<>());

        // Act
        profile.setCsrfProtection("double-submit-cookie");
        List<String> errors1 = profile.getErrors(null);
        profile.setCsrfProtection("samesite-strict-cookie");
        List<String> errors2 = profile.getErrors(null);
        profile.setCsrfProtection("none");
        List<String> errors3 = profile.getErrors(null);

        // Assert
        assertEquals(0, errors1.size());
    }


    @Test
    public void testGetErrorsNoSettings() {

        // Arrange
        SecurityProfile profile = new SecurityProfile();

        // Act
        List<String> errors = profile.getErrors(null);

        // Assert
        assertEquals(2, errors.size());
    }

}