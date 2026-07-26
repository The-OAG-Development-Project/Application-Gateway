package org.owasp.oag.cookies;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * Writes {@link ResponseCookie} instances to a servlet response.
 */
public final class CookieWriter {

    private CookieWriter() {
    }

    /**
     * Adds the cookie to the response as a Set-Cookie header, preserving all of its attributes
     * including SameSite.
     *
     * @param response the response to write to
     * @param cookie   the cookie to add
     */
    public static void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
