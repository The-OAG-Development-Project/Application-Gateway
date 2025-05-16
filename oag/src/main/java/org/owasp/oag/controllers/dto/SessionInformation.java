package org.owasp.oag.controllers.dto;

/**
 * Data transfer object for session information
 * Contains information about the current session state and expiration
 */
public class SessionInformation {

    /**
     * Constant representing an authenticated session state
     */
    public static final String SESSION_STATE_AUTHENTICATED = "authenticated";

    /**
     * Constant representing an anonymous session state
     */
    public static final String SESSION_STATE_ANONYMOUS = "anonymous";

    private String state;
    private int expiresIn;


    /**
     * Constructs a SessionInformation object with the specified state
     *
     * @param state The session state (authenticated or anonymous)
     */
    public SessionInformation(String state) {
        this.state = state;
    }

    /**
     * Gets the current session state
     *
     * @return The session state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the session state
     *
     * @param state The new session state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the time in seconds until the session expires
     *
     * @return The expiration time in seconds
     */
    public int getExpiresIn() {
        return expiresIn;
    }

    /**
     * Sets the time in seconds until the session expires
     *
     * @param expiresIn The expiration time in seconds
     */
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
