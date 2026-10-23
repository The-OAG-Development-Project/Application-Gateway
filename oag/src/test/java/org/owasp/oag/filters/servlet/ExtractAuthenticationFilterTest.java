package org.owasp.oag.filters.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.services.crypto.JweEncrypter;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the filter publishes the session and the csrf token of a login cookie in the request
 * attributes, and that a blacklisted session is not published.
 */
class ExtractAuthenticationFilterTest {

    private static final String CSRF_TOKEN = "the-csrf-token";
    private static final String SESSION_ID = "the-session-id";

    private final StubSessionBlacklist sessionBlacklist = new StubSessionBlacklist();
    private final GlobalClockSource clockSource = new GlobalClockSource();

    private ExtractAuthenticationFilter filter;
    private CookieEncryptor cookieEncryptor;

    @BeforeEach
    void setUp() throws Exception {

        cookieEncryptor = JweEncrypter.loadInMemoryInstance();

        filter = new ExtractAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "cookieEncryptor", cookieEncryptor);
        ReflectionTestUtils.setField(filter, "globalClockSource", clockSource);
        ReflectionTestUtils.setField(filter, "sessionBlacklist", sessionBlacklist);
    }

    @Test
    void publishesAnEmptySessionWhenThereIsNoLoginCookie() throws Exception {

        var request = new MockHttpServletRequest("GET", "/foo");

        doFilter(request, () -> {
            assertEquals(Optional.empty(), Session.REQUEST_SESSION.get());
            assertTrue(Session.REQUEST_SESSION.get().isEmpty());
        });
    }

    @Test
    void publishesTheSessionAndCsrfTokenOfAValidLoginCookie() throws Exception {

        var request = new MockHttpServletRequest("GET", "/foo");
        request.setCookies(loginCookie());

        doFilter(request, () -> {
            Optional<Session> session = Session.REQUEST_SESSION.get();
            assertTrue(session.isPresent(), "session was not published");
            assertEquals(SESSION_ID, session.get().getId());
            assertEquals(CSRF_TOKEN, CsrfCookie.REQUEST_CSRF_TOKEN.get());
        });
    }

    @Test
    void publishesNoSessionWhenTheSessionIsBlacklisted() throws Exception {

        sessionBlacklist.invalidated = true;

        var request = new MockHttpServletRequest("GET", "/foo");
        request.setCookies(loginCookie());

        doFilter(request, () -> assertTrue(Session.REQUEST_SESSION.get().isEmpty(),
          "a blacklisted session must not be published"));
    }

    private void doFilter(MockHttpServletRequest request, Runnable checkInFilter) throws Exception {

        var chain = new MockFilterChain(){
            @Override
            public void doFilter(@NonNull ServletRequest request, @NonNull ServletResponse response) throws IOException, ServletException {
                checkInFilter.run();
                super.doFilter(request, response);
            }
        };
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), "the filter did not continue the chain");
    }

    private Cookie loginCookie() {

        int sessionExp = clockSource.getEpochSeconds() + 3600;
        var loginCookie = new LoginCookie(sessionExp, "local", new UserModel("user-1"), SESSION_ID);
        loginCookie.setCsrfToken(CSRF_TOKEN);

        return new Cookie(LoginCookie.NAME, cookieEncryptor.encryptObject(loginCookie));
    }

    /**
     * Blacklist whose answer the test controls.
     */
    private static class StubSessionBlacklist implements SessionBlacklist {

        private boolean invalidated = false;

        @Override
        public void invalidateSession(String identifier, int ttl) {
        }

        @Override
        public boolean isInvalidated(String identifier) {
            return invalidated;
        }

        @Override
        public void cleanup() {
        }

        @Override
        public void close() {
        }
    }
}
