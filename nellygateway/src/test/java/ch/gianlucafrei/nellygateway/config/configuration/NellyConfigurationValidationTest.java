package ch.gianlucafrei.nellygateway.config.configuration;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NellyConfigurationValidationTest {

    @Test
    public void testValidConfiguration() {

        // Arrange
        SessionBehaviour sessionBehaviour = new SessionBehaviour(
                3600,
                300,
                "/",
                "/",
                "/");
        NellyConfig config = new NellyConfig(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                "hosturi.org",
                null,
                new ArrayList<>(),
                sessionBehaviour);

        // Act
        var errors = config.getErrors(null);

        // Assert
        assertEquals(0, errors.size());
    }

    @Test
    public void testRecursiveConfiguration() {

        // Arrange
        SessionBehaviour sessionBehaviour = new SessionBehaviour(
                -1, // inner error
                300,
                "/",
                "/",
                "/");
        NellyConfig config = new NellyConfig(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                "hosturi.org",
                null,
                new ArrayList<>(),
                sessionBehaviour);

        // Act
        var errors = config.getErrors(null);

        // Assert
        assertEquals(1, errors.size());
    }

    @Test
    public void testUndefinedSecurityProfile() {

        // Arrange
        String undefinedSecurityProfileName = "doesnotexistABC";
        Map<String, NellyRoute> routes = new HashMap<>();
        routes.put("route1", new NellyRoute("/**", "http://backend", undefinedSecurityProfileName, true));
        SessionBehaviour sessionBehaviour = new SessionBehaviour(
                3600, // inner error
                300,
                "/",
                "/",
                "/");
        NellyConfig config = new NellyConfig(
                new HashMap<>(),
                routes,
                new HashMap<>(),
                "hosturi.org",
                null,
                new ArrayList<>(),
                sessionBehaviour);

        // Act
        var errors = config.getErrors(null);

        // Assert
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains(undefinedSecurityProfileName));
    }

    @Test
    public void testNoFields() {

        // Arrange
        NellyConfig config = new NellyConfig(
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Act
        var errors = config.getErrors(null);

        // Assert
        assertEquals(6, errors.size());
    }


}