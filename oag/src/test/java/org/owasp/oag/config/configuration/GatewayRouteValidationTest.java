package org.owasp.oag.config.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayRouteValidationTest {

    @Test
    public void testValidConfiguration() {

        // Arrange
        GatewayRoute route = new GatewayRoute(
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
        GatewayRoute route = new GatewayRoute(
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
        GatewayRoute route = new GatewayRoute(
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