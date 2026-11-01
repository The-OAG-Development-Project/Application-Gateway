package org.owasp.oag.services.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SecurityProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.owasp.oag.extension.TypedExtensionStub.stub;

class CsrfProtectionValidatorTest {

    private final CsrfProtectionValidator validator = new CsrfProtectionValidator(
            new CsrfValidationRegistry(List.of(stub(CsrfProtectionValidation.class, "none"))));

    private static MainConfig configWithCsrfProtection(String csrfProtection) {
        var profile = new SecurityProfile();
        profile.setCsrfProtection(csrfProtection);
        return new MainConfig(Map.of(), Map.of(), new HashMap<>(Map.of("webapp", profile)), null, null, null, null, null);
    }

    @Test
    void knownCsrfProtectionIsValid() {
        assertEquals(List.of(), validator.validate(configWithCsrfProtection("none")));
    }

    @Test
    void unknownCsrfProtectionIsReportedWithSecurityProfile() {
        assertEquals(List.of("CsrfProtection of security profile 'webapp': Unknown type 'x'. Known types: [none]"),
                validator.validate(configWithCsrfProtection("x")));
    }
}
