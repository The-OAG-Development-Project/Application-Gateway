package org.owasp.oag.config.configuration;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainConfigurationValidationTest {

    private final TraceProfile defaultTraceProfile = new TraceProfile(false, 254, false, 254, false, "w3cTrace", null);
    private final KeyManagementProfile defaultKeyManagementProfile = new KeyManagementProfile(new JwkStoreProfile("localRsaJwkStore", new HashMap<>()), new KeyGeneratorProfile("rsaKeyGenerator", 4096, new HashMap<>()), new KeyRotationProfile("defaultKeyRotation", true, 1, 1000));

    @Test
    public void testValidConfiguration() {

        // Arrange
        SessionBehaviour sessionBehaviour = new SessionBehaviour(
                3600,
                300,
                "/",
                "/",
                "/");


        MainConfig config = new MainConfig(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                "https://hosturi.org",
                new ArrayList<>(),
                sessionBehaviour,
                defaultTraceProfile,
                defaultKeyManagementProfile);

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
        MainConfig config = new MainConfig(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                "https://hosturi.org",
                new ArrayList<>(),
                sessionBehaviour,
                defaultTraceProfile,
                defaultKeyManagementProfile);

        // Act
        var errors = config.getErrors(null);

        // Assert
        assertEquals(1, errors.size());
    }

    @Test
    public void testUndefinedSecurityProfile() {

        // Arrange
        String undefinedSecurityProfileName = "doesnotexistABC";
        Map<String, GatewayRoute> routes = new HashMap<>();
        routes.put("route1", new GatewayRoute("/**", "http://backend", undefinedSecurityProfileName, true));
        SessionBehaviour sessionBehaviour = new SessionBehaviour(
                3600, // inner error
                300,
                "/",
                "/",
                "/");
        MainConfig config = new MainConfig(
                new HashMap<>(),
                routes,
                new HashMap<>(),
                "https://hosturi.org",
                new ArrayList<>(),
                sessionBehaviour,
                defaultTraceProfile,
                defaultKeyManagementProfile);

        // Act
        var errors = config.getErrors(null);

        // Assert
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains(undefinedSecurityProfileName));
    }

    @Test
    public void testNoFields() {

        // Arrange
        MainConfig config = new MainConfig(
                null,
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
        assertEquals(8, errors.size());
    }


}