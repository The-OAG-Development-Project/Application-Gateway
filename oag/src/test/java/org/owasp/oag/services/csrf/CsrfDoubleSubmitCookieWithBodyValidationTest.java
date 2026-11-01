package org.owasp.oag.services.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the double submit cookie validation with body compares the csrf token from the body, header or
 * query string with the token of the session.
 */
class CsrfDoubleSubmitCookieWithBodyValidationTest {

    private static final String CSRF_TOKEN = "the-csrf-token";

    private final CsrfDoubleSubmitCookieWithBodyValidation validation = new CsrfDoubleSubmitCookieWithBodyValidation();

    @Test
    void acceptsABodyContainingTheTokenOfTheSession() {

        var request = new MockHttpServletRequest("POST", "/foo");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "CSRFToken=" + CSRF_TOKEN)));
    }

    @Test
    void acceptsAHeaderTokenMatchingTheTokenOfTheSession() {

        var request = headerRequest(CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void blocksAHeaderTokenDifferentFromTheTokenOfTheSession() {

        var request = headerRequest("another-token");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void blocksARequestWhenTheSessionHasNoToken() {

        var request = headerRequest(CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(null))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, CSRF_TOKEN)));
    }

    @Test
    void acceptsARequestWithoutSession() {

        var request = new MockHttpServletRequest("POST", "/foo");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.<Session>empty()).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void rejectsAMissingBodyWhenThereIsASession() {

        var request = headerRequest(CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertThrows(AssertionError.class, () -> validation.shouldBlockRequest(request, null)));
    }

    private MockHttpServletRequest headerRequest(String csrfToken) {
        var request = new MockHttpServletRequest("POST", "/foo");
        request.addHeader(CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_HEADER_NAME, csrfToken);
        return request;
    }

    private Session session(String csrfToken) {
        return new Session(300, 300, "IAM", new UserModel("alice"), csrfToken, "sessionId");
    }
}
