package org.owasp.oag.config.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginProviderValidationTest {

    @Test
    void missingTypeAndSettingsAreReported() {
        assertEquals(List.of("LoginProvider: No type defined", "LoginProvider: No settings defined"),
                new LoginProvider(null, null).getErrors());
    }

    @Test
    void anyTypeWithSettingsIsStructurallyValid() {
        assertEquals(List.of(), new LoginProvider("anything", new LoginProviderSettings()).getErrors());
    }
}
