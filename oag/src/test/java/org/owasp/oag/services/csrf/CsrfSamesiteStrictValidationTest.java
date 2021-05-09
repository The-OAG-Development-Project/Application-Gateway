package org.owasp.oag.services.csrf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfSamesiteStrictValidationTest {

    @Test
    void shouldBlockBasedOnOriginHeaderFalse() {

        var validator = new CsrfSamesiteStrictValidation();

        // Happy case
        assertFalse(validator.shouldBlockBasedOnOriginHeader("https://oag.azurewebsites.net", null, "https://oag.azurewebsites.net/"));

        // No origin or referer header, In doubt we should not block the request
        assertFalse(validator.shouldBlockBasedOnOriginHeader(null, null, "https://oag.azurewebsites.net/"));
        assertFalse(validator.shouldBlockBasedOnOriginHeader("null", null, "https://oag.azurewebsites.net/"));

        // No origin or referer header, In doubt we should not block the request
        assertFalse(validator.shouldBlockBasedOnOriginHeader(null, null, null));

        // Browser might only send the referer header
        assertFalse(validator.shouldBlockBasedOnOriginHeader(null, "https://oag.azurewebsites.net/abc", "https://oag.azurewebsites.net/"));
    }

    @Test
    void shouldBlockBasedOnOriginHeaderTrue() {

        var validator = new CsrfSamesiteStrictValidation();

        // Different origin
        assertTrue(validator.shouldBlockBasedOnOriginHeader("https://badwebsite.com", null, "https://oag.azurewebsites.net/"));
        assertTrue(validator.shouldBlockBasedOnOriginHeader("https://badsubdomain.oag.azurewebsites.net", null, "https://oag.azurewebsites.net/"));

        // Different referer
        assertTrue(validator.shouldBlockBasedOnOriginHeader(null, "https://badsubdomain.oag.azurewebsites.net/somepath", "https://oag.azurewebsites.net/"));

        // Different protocol
        assertTrue(validator.shouldBlockBasedOnOriginHeader("http://oag.azurewebsites.net", null, "https://oag.azurewebsites.net/"));
    }
}