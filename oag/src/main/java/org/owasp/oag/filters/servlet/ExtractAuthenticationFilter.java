package org.owasp.oag.filters.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.exception.CookieDecryptionException;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
     * Extracts the session that this filter stored in the request attributes.
     *
     * @param request The current request
     * @return An Optional containing the Session if found and valid, empty otherwise
     */
    @SuppressWarnings("unchecked")
    public static Optional<Session> extractSessionFromRequest(HttpServletRequest request) {

        Object sessionOptional = request.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional == null)
            return Optional.empty();

        if (sessionOptional instanceof Optional) {
            //noinspection unchecked
            return (Optional<Session>) sessionOptional;
        } else {
            LoggingUtils.logWarn(log, request, "oag-session attribute is of incompatible type!! Fix it.");
            return Optional.empty();
        }
    }

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, java.io.IOException {

        LoggingUtils.logTrace(log, request, "Execute ExtractAuthenticationFilter");

        extractAndStoreAuthentication(request);
        chain.doFilter(request, response);
    }

    /**
     * Extracts authentication from the login cookie and stores it in the request attributes.
     * Also checks if the session has been invalidated.
     *
     * @param request The current request
     */
    protected void extractAndStoreAuthentication(HttpServletRequest request) {

        request.setAttribute(OAG_SESSION, Optional.empty());

        var loginCookieOptional = extractLoginCookieFromRequest(request);
        if (loginCookieOptional.isEmpty())
            return;

        var loginCookie = loginCookieOptional.get();

        if (sessionBlacklist.isInvalidated(loginCookie.getId())) {
            LoggingUtils.logInfo(log, request, "Received invalidated session cookie with id: {} user-id: {}",
                    loginCookie.getId(), loginCookie.getUserModel().getId());
            return;
        }

        var sessionOptional = Session.fromSessionCookie(loginCookie, globalClockSource.getGlobalClock());

        if (sessionOptional.isEmpty()) {
            LoggingUtils.logDebug(log, request, "Received invalid session cookie, maybe expired");
        }

        // Extract csrf token from session cookie and store in the request
        request.setAttribute(OAG_SESSION_CSRF_TOKEN, loginCookie.getCsrfToken());
        // Store session optional in http request object
        request.setAttribute(OAG_SESSION, sessionOptional);
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

            LoggingUtils.logInfo(log, request, "Received invalid session cookie");
        }

        return Optional.ofNullable(loginCookie);
    }
}
