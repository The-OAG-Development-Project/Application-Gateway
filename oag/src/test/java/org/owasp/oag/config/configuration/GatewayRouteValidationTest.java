package org.owasp.oag.config.configuration;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayRouteValidationTest {

    @Test
    public void testValidConfiguration() {

        // Arrange
        GatewayRoute route = new GatewayRoute(
                "/abc/**",
                "https://backend/abc",
                "thetype",
                false,
                null);

        // Act
        var errors = route.getErrors(null, null);

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
                false,
                null);

        // Act
        var errors = route.getErrors(null, null);

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
                false,
                null);

        // Act
        var errors = route.getErrors(null, null);

        // Assert
        assertEquals(1, errors.size());
    }

    @Test
    public void testNonexistentAutoLoginValue(){

        // Arrange
        GatewayRoute routeInvalid = new GatewayRoute(
                "/abc/**",
                "https://backend/abc",
                "thetype",
                false,
                null,
                "doesnotexist");
        GatewayRoute routeValid = new GatewayRoute(
                "/abc/**",
                "https://backend/abc",
                "thetype",
                false,
                null,
                "exists");


        Map<String, LoginProvider> providers = new HashMap<>();
        providers.put("exists", null);
        MainConfig config = new MainConfig(providers, null, null, null, null, null, null, null);

        // Act
        var errorsInvalid = routeInvalid.getErrors(null, config);
        var errorsValid = routeValid.getErrors(null, config);

        // Assert
        assertEquals(1, errorsInvalid.size());
        assertEquals(0, errorsValid.size());
    }
}