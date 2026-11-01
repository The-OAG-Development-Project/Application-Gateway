package org.owasp.oag.services.csrf;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.services.crypto.JweEncrypter;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfSameSiteStrictCookieValidationTest {

    private static final String CSRF_TOKEN = "the-csrf-token";

    private final CsrfSameSiteStrictCookieValidation validation = new CsrfSameSiteStrictCookieValidation();

    @BeforeEach
    void setUp() throws Exception {

        var mainConfig = new MainConfig();
        validation.mainConfig = mainConfig;
        validation.cookieConverter = new CookieConverter(mainConfig, JweEncrypter.loadInMemoryInstance());
    }

    @Test
    void acceptsACsrfCookieMatchingTheTokenOfTheSession() {

        var request = cookieRequest(CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertFalse(validation.shouldBlockRequest(request, null)));
    }

    @Test
    void blocksACsrfCookieDifferentFromTheTokenOfTheSession() {

        var request = cookieRequest("another-token");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(CSRF_TOKEN))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, null)));
    }

    @Test
    void blocksARequestWhenTheSessionHasNoToken() {

        var request = cookieRequest(CSRF_TOKEN);

        ScopedValue.where(Session.REQUEST_SESSION, Optional.of(session(null))).run(() ->
            assertTrue(validation.shouldBlockRequest(request, null)));
    }

    @Test
    void acceptsARequestWithoutSession() {

        var request = new MockHttpServletRequest("POST", "/foo");

        ScopedValue.where(Session.REQUEST_SESSION, Optional.<Session>empty()).run(() ->
            assertFalse(validation.shouldBlockRequest(request, null)));
    }

    @Test
    void shouldBlockBasedOnOriginHeaderFalse() {

        var validator = new CsrfSameSiteStrictCookieValidation();

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

        var validator = new CsrfSameSiteStrictCookieValidation();

        // Different origin
        assertTrue(validator.shouldBlockBasedOnOriginHeader("https://badwebsite.com", null, "https://oag.azurewebsites.net/"));
        assertTrue(validator.shouldBlockBasedOnOriginHeader("https://badsubdomain.oag.azurewebsites.net", null, "https://oag.azurewebsites.net/"));

        // Different referer
        assertTrue(validator.shouldBlockBasedOnOriginHeader(null, "https://badsubdomain.oag.azurewebsites.net/somepath", "https://oag.azurewebsites.net/"));

        // Different protocol
        assertTrue(validator.shouldBlockBasedOnOriginHeader("http://oag.azurewebsites.net", null, "https://oag.azurewebsites.net/"));
    }

    private MockHttpServletRequest cookieRequest(String csrfToken) {
        var request = new MockHttpServletRequest("POST", "/foo");
        request.setCookies(new Cookie(CsrfCookie.NAME, csrfToken));
        return request;
    }

    private Session session(String csrfToken) {
        return new Session(300, 300, "IAM", new UserModel("alice"), csrfToken, "sessionId");
    }
}
