package org.owasp.oag.cookies;

/**
 * Represents a cookie that stores the state information during the login process.
 * This cookie is used to maintain state between authentication redirects, storing
 * information about the authentication provider, state token, and the return URL.
 */
public class LoginStateCookie {

    /** The name of the cookie used for state storage */
    public static final String NAME = "state";

    /** The authentication provider identifier */
    private String provider;
    
    /** The state token used to verify the authentication response */
    private String state;
    
    /** The URL to return to after authentication is complete */
    private String returnUrl;

    /**
     * Default constructor for deserialization purposes.
     */
    public LoginStateCookie() {
    }

    /**
     * Creates a new login state cookie with all required information.
     *
     * @param provider The authentication provider identifier
     * @param state The state token used to verify the authentication response
     * @param returnUrl The URL to return to after authentication is complete
     */
    public LoginStateCookie(String provider, String state, String returnUrl) {
        this.provider = provider;
        this.state = state;
        this.returnUrl = returnUrl;
    }

    /**
     * Gets the authentication provider identifier.
     *
     * @return The provider identifier
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the authentication provider identifier.
     *
     * @param provider The provider identifier
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Gets the state token used to verify the authentication response.
     *
     * @return The state token
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state token used to verify the authentication response.
     *
     * @param state The state token
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the URL to return to after authentication is complete.
     *
     * @return The return URL
     */
    public String getReturnUrl() {
        return returnUrl;
    }

    /**
     * Sets the URL to return to after authentication is complete.
     *
     * @param returnUrl The return URL
     */
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
