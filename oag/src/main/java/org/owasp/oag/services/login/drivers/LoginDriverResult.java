package org.owasp.oag.services.login.drivers;

import java.net.URI;

/**
 * Represents the result of a login driver operation containing authentication URI and state.
 * Used to store and transfer authentication-related information during the login process.
 */
public class LoginDriverResult {

    /** The URI where the user should be redirected for authentication */
    private URI authURI;
    /** The state parameter used to maintain session state between request and callback */
    private String state;

    /**
     * Constructs a new LoginDriverResult with the specified authentication URI and state.
     * @param authURI The URI where the user should be redirected for authentication
     * @param state The state parameter to maintain session state
     */
    public LoginDriverResult(URI authURI, String state) {
        this.authURI = authURI;
        this.state = state;
    }

    /**
     * Gets the authentication URI.
     * @return The URI where the user should be redirected for authentication
     */
    public URI getAuthURI() {
        return authURI;
    }

    /**
     * Sets the authentication URI.
     * @param authURI The URI where the user should be redirected for authentication
     */
    public void setAuthURI(URI authURI) {
        this.authURI = authURI;
    }

    /**
     * Gets the state parameter.
     * @return The state parameter used to maintain session state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state parameter.
     * @param state The state parameter used to maintain session state
     */
    public void setState(String state) {
        this.state = state;
    }
}
