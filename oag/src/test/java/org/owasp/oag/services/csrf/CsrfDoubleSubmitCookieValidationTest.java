package org.owasp.oag.services.csrf;

import org.junit.jupiter.api.Test;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies how the double submit cookie validation reads the csrf token from the query string, in
 * particular that a query string it cannot parse blocks the request instead of producing an error.
 */
class CsrfDoubleSubmitCookieValidationTest {

    private static final String CSRF_TOKEN = "the-csrf-token";

    private final CsrfDoubleSubmitCookieValidation validation = new CsrfDoubleSubmitCookieValidation();

    @Test
    void acceptsAMatchingTokenFromTheQueryString() {

        var request = createPostRequest("CSRFToken=" + CSRF_TOKEN);


        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session())).
          where(CsrfCookie.REQUEST_CSRF_TOKEN, CSRF_TOKEN).run(() ->
            assertFalse(validation.shouldBlockRequest(request, "")));
    }

    @Test
    void blocksARequestWhoseQueryStringHasAnInvalidPercentEscape() {

        var request = createPostRequest("CSRFToken=%zz");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session())).
          where(CsrfCookie.REQUEST_CSRF_TOKEN, CSRF_TOKEN).run(() ->
            assertTrue(validation.shouldBlockRequest(request, ""),
                "an unparseable token must be treated as absent, not raise an error"));
    }

    @Test
    void blocksARequestWhoseQueryStringHasATrailingPercentSign() {

        var request = createPostRequest("CSRFToken=" + CSRF_TOKEN + "%");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session())).
          where(CsrfCookie.REQUEST_CSRF_TOKEN, CSRF_TOKEN).run(() ->
            assertTrue(validation.shouldBlockRequest(request, ""),
                "an unparseable token must be treated as absent, not raise an error"));
    }

    private MockHttpServletRequest createPostRequest(String queryString) {
        var request = new MockHttpServletRequest("POST", "/foo");
        request.setQueryString(queryString);
        return request;
    }

    private Session session() {
        return new Session(300, 300, "IAM", new UserModel("alice"), null, "sessionId");
    }
}
