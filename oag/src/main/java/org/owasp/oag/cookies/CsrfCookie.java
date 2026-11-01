package org.owasp.oag.cookies;

/**
 * Represents a CSRF (Cross-Site Request Forgery) cookie.
 * This class encapsulates the CSRF token used to protect against CSRF attacks
 * by ensuring that requests originate from the same site.
 */
public class CsrfCookie {

    /**
     * The name of the CSRF cookie used in HTTP requests and responses.
     * This constant defines the standard name for CSRF cookies in the application.
     */
    public static final String NAME = "csrf";

    private String csrfToken;

    /**
     * Constructor for CsrfCookie.
     *
     * @param csrfToken The CSRF token.
     */
    public CsrfCookie(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    /**
     * Gets the CSRF token.
     *
     * @return The CSRF token.
     */
    public String getCsrfToken() {
        return csrfToken;
    }

    /**
     * Sets the CSRF token.
     *
     * @param csrfToken The CSRF token to set.
     */
    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }
}
