package org.owasp.oag.cookies;

import org.owasp.oag.session.UserModel;

/**
 * Represents the content of the login cookie used to maintain user sessions.
 * This cookie contains authentication information including the user model,
 * session expiration time, provider information, and CSRF protection token.
 */
public class LoginCookie {

    /**
     * The name of the cookie used for session storage.
     */
    public static final String NAME = "session";

    /**
     * Session expiration time in seconds.
     */
    private int sessionExpSeconds;
    
    /**
     * The key of the authentication provider that authenticated the user.
     */
    private String providerKey;
    
    /**
     * The user model containing authenticated user information.
     */
    private UserModel userModel;
    
    /**
     * Cross-Site Request Forgery (CSRF) protection token.
     */
    private String csrfToken;
    
    /**
     * Unique identifier for this session.
     */
    private String id;

    /**
     * Default constructor for deserialization purposes.
     */
    public LoginCookie() {
    }

    /**
     * Creates a new login cookie with the specified session information.
     *
     * @param sessionExpSeconds The session expiration time in seconds
     * @param provider The key of the authentication provider
     * @param userModel The authenticated user model
     * @param id The unique session identifier
     */
    public LoginCookie(int sessionExpSeconds, String provider, UserModel userModel, String id) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.providerKey = provider;
        this.userModel = userModel;
        this.id = id;
    }

    /**
     * Gets the session expiration time in seconds.
     *
     * @return The session expiration time in seconds
     */
    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    /**
     * Sets the session expiration time in seconds.
     *
     * @param sessionExpSeconds The session expiration time in seconds
     */
    public void setSessionExpSeconds(int sessionExpSeconds) {
        this.sessionExpSeconds = sessionExpSeconds;
    }

    /**
     * Gets the authentication provider key.
     *
     * @return The authentication provider key
     */
    public String getProviderKey() {
        return providerKey;
    }

    /**
     * Sets the authentication provider key.
     *
     * @param providerKey The authentication provider key
     */
    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    /**
     * Gets the authenticated user model.
     *
     * @return The user model
     */
    public UserModel getUserModel() {
        return userModel;
    }

    /**
     * Sets the authenticated user model.
     *
     * @param userModel The user model
     */
    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    /**
     * Gets the CSRF protection token.
     *
     * @return The CSRF token
     */
    public String getCsrfToken() {
        return csrfToken;
    }

    /**
     * Sets the CSRF protection token.
     *
     * @param csrfToken The CSRF token
     */
    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    /**
     * Gets the unique session identifier.
     *
     * @return The session identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique session identifier.
     *
     * @param id The session identifier
     */
    public void setId(String id) {
        this.id = id;
    }
}
