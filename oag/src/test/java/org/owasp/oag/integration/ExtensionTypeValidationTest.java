package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.owasp.oag.config.FileConfigLoader;
import org.owasp.oag.config.MainConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
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

    @Autowired
    MainConfigValidator validator;

    private static MainConfig sampleConfig() throws IOException {
        return new FileConfigLoader("sample-config.yaml").loadConfiguration();
    }

    @Test
    void sampleConfigurationIsValid() throws IOException {
        var config = new FileConfigLoader("sample-config.yaml").loadConfiguration();

        assertEquals(List.of(), validator.validate(config));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/defaultKeyRotationConfiguration.yaml", "/headerUserMappingConfig.yaml",
            "/localServerConfiguration.yaml", "/noTracingConfiguration.yaml", "/rewriteTestConfig.yaml",
            "/simpleTracingConfiguration.yaml", "/W3CTracingConfiguration.yaml"})
    void testConfigurationIsValid(String resource) throws IOException {
        var config = new TestFileConfigLoader(resource).loadConfiguration();

        assertEquals(List.of(), validator.validate(config));
    }

    @Test
    void unknownUserMappingTypeIsReported() throws IOException {
        var config = sampleConfig();
        config.getSecurityProfiles().get("webapplication").getUserMapping().setType("doesnotexist");

        var errors = validator.validate(config);

        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("'webapplication'"), errors.getFirst());
        assertTrue(errors.getFirst().contains("'doesnotexist'"), errors.getFirst());
        assertTrue(errors.getFirst().contains("[jwtToken, no, requestHeader]"), errors.getFirst());
    }

    @Test
    void unknownCsrfProtectionIsReported() throws IOException {
        var config = sampleConfig();
        config.getSecurityProfiles().get("webapplication").setCsrfProtection("doesnotexist");

        var errors = validator.validate(config);

        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("'webapplication'"), errors.getFirst());
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
    void unknownKeyManagementTypesAreReported() throws IOException {
        var config = sampleConfig();
        var keyManagement = config.getKeyManagementProfile();
        keyManagement.getJwkStoreProfile().setType("doesnotexist");
        keyManagement.getKeyGeneratorProfile().setType("doesnotexist");
        keyManagement.getKeyRotationProfile().setType("doesnotexist");

        var errors = validator.validate(config);

        assertEquals(3, errors.size(), "Errors: " + errors);
        assertTrue(errors.get(0).contains("[localRsaJwkStore]"), errors.get(0));
        assertTrue(errors.get(1).contains("[rsaKeyGenerator]"), errors.get(1));
        assertTrue(errors.get(2).contains("[defaultKeyRotation]"), errors.get(2));
    }

    @Test
    void unknownTraceTypeIsReported() throws IOException {
        var config = sampleConfig();
        config.getTraceProfile().setType("doesnotexist");

        assertUnknownTypeReported(validator.validate(config), "[noTraceContext, simpleTraceContext, w3cTraceContext]");
    }

    private static void assertUnknownTypeReported(List<String> errors, String knownTypes) {
        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("'doesnotexist'"), errors.getFirst());
        assertTrue(errors.getFirst().contains(knownTypes), errors.getFirst());
    }
}
