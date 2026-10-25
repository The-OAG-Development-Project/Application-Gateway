package org.owasp.oag.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.owasp.oag.config.configuration.UserMappingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

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
}
