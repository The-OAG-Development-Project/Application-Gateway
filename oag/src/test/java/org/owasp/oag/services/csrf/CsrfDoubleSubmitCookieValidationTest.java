package org.owasp.oag.services.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the double submit cookie validation compares the submitted csrf token with the token of the
 * session, and that a query string it cannot parse blocks the request instead of producing an error.
 */
class CsrfDoubleSubmitCookieValidationTest {

    private static final String CSRF_TOKEN = "the-csrf-token";

    private final CsrfDoubleSubmitCookieValidation validation = new CsrfDoubleSubmitCookieValidation();

    @Test
    void acceptsAMatchingTokenFromTheQueryString() {

        var request = createPostRequest("CSRFToken=" + CSRF_TOKEN);


        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void blocksARequestWhoseQueryStringHasAnInvalidPercentEscape() {

        var request = createPostRequest("CSRFToken=%zz");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, ""),
                "an unparseable token must be treated as absent, not raise an error"));
    }

    @Test
    void blocksARequestWhoseQueryStringHasATrailingPercentSign() {

        var request = createPostRequest("CSRFToken=" + CSRF_TOKEN + "%");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, ""),
                "an unparseable token must be treated as absent, not raise an error"));
    }

    @Test
    void acceptsAHeaderTokenMatchingTheTokenOfTheSession() {

        var request = createPostRequest(null);
        request.addHeader(CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_HEADER_NAME, CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void blocksAHeaderTokenDifferentFromTheTokenOfTheSession() {

        var request = createPostRequest(null);
        request.addHeader(CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_HEADER_NAME, "another-token");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void blocksARequestWhenTheSessionHasNoToken() {

        var request = createPostRequest(null);
        request.addHeader(CsrfDoubleSubmitCookieValidation.CSRF_TOKEN_HEADER_NAME, CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(null))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void acceptsARequestWithoutSession() {

        var request = createPostRequest(null);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.<Session>empty()).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "")));
    }

    private MockHttpServletRequest createPostRequest(String queryString) {
        var request = new MockHttpServletRequest("POST", "/foo");
        request.setQueryString(queryString);
        return request;
    }

    private Session session(String csrfToken) {
        return new Session(300, 300, "IAM", new UserModel("alice"), csrfToken, "sessionId");
    }
}
