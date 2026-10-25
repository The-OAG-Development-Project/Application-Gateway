package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.owasp.oag.config.FileConfigLoader;
import org.owasp.oag.config.configuration.JwkStoreProfile;
import org.owasp.oag.config.configuration.KeyGeneratorProfile;
import org.owasp.oag.config.configuration.KeyRotationProfile;
import org.owasp.oag.config.configuration.SecurityProfile;
import org.owasp.oag.config.configuration.TraceProfile;
import org.owasp.oag.config.configuration.UserMappingConfig;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.services.keymgm.JwkStoreRegistry;
import org.owasp.oag.services.keymgm.KeyGeneratorRegistry;
import org.owasp.oag.services.keymgm.KeyRotationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that configuration validation reports unknown extension types with the known types.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true", "logging.level.org.owasp.oag=TRACE"})
class ExtensionTypeValidationTest {

    @Autowired
    ApplicationContext context;

    @Test
    void sampleConfigurationIsValid() throws IOException {
        var config = new FileConfigLoader("sample-config.yaml").loadConfiguration();

        assertEquals(List.of(), config.getErrors(context));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/defaultKeyRotationConfiguration.yaml", "/headerUserMappingConfig.yaml",
            "/localServerConfiguration.yaml", "/noTracingConfiguration.yaml", "/rewriteTestConfig.yaml",
            "/simpleTracingConfiguration.yaml", "/W3CTracingConfiguration.yaml"})
    void testConfigurationIsValid(String resource) throws IOException {
        var config = new TestFileConfigLoader(resource).loadConfiguration();

        assertEquals(List.of(), config.getErrors(context));
    }

    @Test
    void unknownUserMappingTypeIsReported() {
        var userMapping = new UserMappingConfig();
        userMapping.setType("doesnotexist");
        userMapping.setSettings(Map.of());

        var errors = userMapping.getErrors(context);

        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("'doesnotexist'"), errors.getFirst());
        assertTrue(errors.getFirst().contains("[jwtToken, no, requestHeader]"), errors.getFirst());
    }

    @Test
    void knownUserMappingTypeIsValid() {
        var userMapping = new UserMappingConfig();
        userMapping.setType("no");

        assertTrue(userMapping.getErrors(context).isEmpty());
    }

    @Test
    void unknownCsrfProtectionIsReported() {
        var userMapping = new UserMappingConfig();
        userMapping.setType("no");
        var profile = new SecurityProfile();
        profile.setAllowedMethods(List.of("GET"));
        profile.setCsrfProtection("doesnotexist");
        profile.setCsrfSafeMethods(List.of("GET"));
        profile.setUserMapping(userMapping);

        var errors = profile.getErrors(context);

        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("'doesnotexist'"), errors.getFirst());
        assertTrue(errors.getFirst().contains("[doubleSubmitCookie, doubleSubmitCookieWithBody, none, sameSiteStrictCookie]"), errors.getFirst());
    }

    @Test
    void keyManagementRegistriesContainBuiltInTypes() {
        assertEquals(Set.of("localRsaJwkStore"), context.getBean(JwkStoreRegistry.class).types());
        assertEquals(Set.of("rsaKeyGenerator"), context.getBean(KeyGeneratorRegistry.class).types());
        assertEquals(Set.of("defaultKeyRotation"), context.getBean(KeyRotationRegistry.class).types());
    }

    @Test
    void unknownKeyManagementTypesAreReported() {
        var jwkStore = new JwkStoreProfile("doesnotexist", Map.of());
        var keyGenerator = new KeyGeneratorProfile("doesnotexist", 2048, Map.of());
        var keyRotation = new KeyRotationProfile("doesnotexist", false, 3600, 3600);

        assertUnknownTypeReported(jwkStore.getErrors(context), "[localRsaJwkStore]");
        assertUnknownTypeReported(keyGenerator.getErrors(context), "[rsaKeyGenerator]");
        assertUnknownTypeReported(keyRotation.getErrors(context), "[defaultKeyRotation]");
    }

    @Test
    void unknownTraceTypeIsReported() {
        var traceProfile = new TraceProfile(true, 128, true, 512, true, "doesnotexist", Map.of());

        assertUnknownTypeReported(traceProfile.getErrors(context), "[noTraceContext, simpleTraceContext, w3cTraceContext]");
    }

    private static void assertUnknownTypeReported(List<String> errors, String knownTypes) {
        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("'doesnotexist'"), errors.getFirst());
        assertTrue(errors.getFirst().contains(knownTypes), errors.getFirst());
    }
}
