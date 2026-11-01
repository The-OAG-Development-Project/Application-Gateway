package org.owasp.oag.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

/**
 * Reads cookies from a servlet request.
 */
public final class CookieReader {

    private CookieReader() {
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
}
