package org.owasp.oag.services.csrf;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CsrfValidationRegistryTest {

    @Test
    void registersBuiltInCsrfProtectionsWithoutDuplicates() {
        var registry = new CsrfValidationRegistry(List.of(
                new CsrfNoneValidation(),
                new CsrfSameSiteStrictCookieValidation(),
                new CsrfDoubleSubmitCookieValidation(),
                new CsrfDoubleSubmitCookieWithBodyValidation()));

        assertEquals(Set.of("none", "sameSiteStrictCookie", "doubleSubmitCookie", "doubleSubmitCookieWithBody"), registry.types());
        assertInstanceOf(CsrfDoubleSubmitCookieWithBodyValidation.class, registry.get("doubleSubmitCookieWithBody"));
    }
}
