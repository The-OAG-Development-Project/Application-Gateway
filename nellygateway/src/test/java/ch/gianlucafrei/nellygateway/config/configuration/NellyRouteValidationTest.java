package ch.gianlucafrei.nellygateway.config.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NellyRouteValidationTest {

    @Test
    public void testValidConfiguration() {

        // Arrange
        NellyRoute route = new NellyRoute(
                "/abc/**",
                "https://backend/abc",
                "thetype",
                false);

        // Act
        var errors = route.getErrors(null);

        // Assert
        assertEquals(0, errors.size());
    }

    @Test
    public void testNoFields() {

        // Arrange
        NellyRoute route = new NellyRoute(
                null,
                null,
                null,
                false);

        // Act
        var errors = route.getErrors(null);

        // Assert
        assertEquals(3, errors.size());
    }

    @Test
    public void testInvalidUrl() {

        // Arrange
        NellyRoute route = new NellyRoute(
                "/abc/**",
                "/abc",
                "thetype",
                false);

        // Act
        var errors = route.getErrors(null);

        // Assert
        assertEquals(1, errors.size());
    }
}