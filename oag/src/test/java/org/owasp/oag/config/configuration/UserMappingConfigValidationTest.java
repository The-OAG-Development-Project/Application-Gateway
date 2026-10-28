package org.owasp.oag.config.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMappingConfigValidationTest {

    @Test
    void missingTypeIsReported() {
        assertEquals(List.of("Config: tokenMapping implementation is not defined"), new UserMappingConfig().getErrors());
    }

    @Test
    void anyTypeIsStructurallyValid() {
        var userMapping = new UserMappingConfig();
        userMapping.setType("anything");

        assertEquals(List.of(), userMapping.getErrors());
    }
}
