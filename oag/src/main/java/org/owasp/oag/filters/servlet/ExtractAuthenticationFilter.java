package org.owasp.oag.filters.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.exception.CookieDecryptionException;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * This filter extracts authentication information from login cookies and makes it available in the request attributes.
 * It validates sessions and handles CSRF token extraction.
 */
@Order(40)
@Component
public class ExtractAuthenticationFilter extends OncePerRequestFilter {

    /** Key for the session in the request */
    public final static String OAG_SESSION = "oag-session";

    /** Key for the CSRF token in the request */
    public final static String OAG_SESSION_CSRF_TOKEN = "oag-session-csrf-token";

    private static final Logger log = LoggerFactory.getLogger(ExtractAuthenticationFilter.class);

    @Autowired
    private CookieEncryptor cookieEncryptor;

    @Autowired
    private GlobalClockSource globalClockSource;

    @Autowired
    private SessionBlacklist sessionBlacklist;

    /**
     * Reads the cookie with the given name from the request.
     *
     * @param request The current request
     * @param name    The name of the cookie
     * @return An Optional containing the first cookie with that name, empty when the request has none
     */
    public static Optional<Cookie> findCookie(HttpServletRequest request, String name) {

        var cookies = request.getCookies();

        if (cookies == null)
            return Optional.empty();

        return Arrays.stream(cookies).filter(cookie -> name.equals(cookie.getName())).findFirst();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, java.io.IOException {

        log.trace("Execute ExtractAuthenticationFilter");
        Optional<Session> session = Optional.empty();
        String csrfToken = null;

        Optional<LoginCookie> loginCookieOptional = extractLoginCookieFromRequest(request);
        if (loginCookieOptional.isPresent()) {
            var loginCookie = loginCookieOptional.get();

            if (sessionBlacklist.isInvalidated(loginCookie.getId())) {
                log.info("Received invalidated session cookie with id: {} user-id: {}",
                  loginCookie.getId(), loginCookie.getUserModel().getId());
                // do not accept session and csrf token
            } else {
                session = Session.fromSessionCookie(loginCookie, globalClockSource.getGlobalClock());
                if (session.isEmpty()) {
                    log.debug("Received invalid session cookie, maybe expired");
                }
                // Extract csrf token from session cookie
                csrfToken = loginCookie.getCsrfToken();
            }
        }
        ScopedValue.where(Session.REQUEST_SESSION, session).
          where(CsrfCookie.REQUEST_CSRF_TOKEN, csrfToken).run(() -> {
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw new ApplicationException("General Exception during request.", e);
            }
        });
    }

    /**
     * Extracts and decrypts the login cookie from the request.
     *
     * @param request The current request
     * @return An Optional containing the LoginCookie if found and successfully decrypted, empty otherwise
     */
    protected Optional<LoginCookie> extractLoginCookieFromRequest(HttpServletRequest request) {

        LoginCookie loginCookie = null;
        var cookieOptional = findCookie(request, LoginCookie.NAME);

        if (cookieOptional.isEmpty())
            return Optional.empty();

        try {
            // Decrypt cookie
            loginCookie = cookieEncryptor.decryptObject(cookieOptional.get().getValue(), LoginCookie.class);
        } catch (CookieDecryptionException e) {
            log.info("Received invalid session cookie (decrypt failed due to: {}).", e.getMessage());
        }

        return Optional.ofNullable(loginCookie);
    }
}
