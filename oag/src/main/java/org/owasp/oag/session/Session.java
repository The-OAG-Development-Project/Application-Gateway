package org.owasp.oag.session;

import java.util.Optional;

/**
 * This class is the main representation of a user session within OAG.
 * The user session information is serialized in encrypted form in the session cookie and extracted
 * when a request is processed.
 *
 * The class also provides access to a requests session by means of the REQUEST_SESSION ScopedValue (that is setup in the ExtractAuthenticationFilter).
 */
public class Session {

    /**
     * The session of a request.
     */
    public static final ScopedValue<Optional<Session>> REQUEST_SESSION = ScopedValue.newInstance();

    private final long sessionExpSeconds;
    private final long remainingTimeSeconds;
    private final String provider;
    private final UserModel userModel;
    private final String csrfToken;
    private final String id;

    /**
     * Creates a new Session object with all required values
     *
     * @param sessionExpSeconds The session expiration time in seconds (Unix timestamp)
     * @param remainingTimeSeconds The remaining time until session expiration in seconds
     * @param provider The name of the login provider that authenticated the user
     * @param userModel The user model containing user information
     * @param csrfToken The CSRF token bound to this session, may be {@code null}
     * @param id The unique identifier for this session
     */
    public Session(long sessionExpSeconds, long remainingTimeSeconds, String provider, UserModel userModel, String csrfToken, String id) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.provider = provider;
        this.userModel = userModel;
        this.csrfToken = csrfToken;
        this.id = id;
    }

    /**
     * Returns the name of the used login provider
     *
     * @return The name of the login provider that authenticated the user
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the end of the session validity as unix timestamp
     *
     * @return The session expiration time in seconds (Unix timestamp)
     */
    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    /**
     * Returns the duration until session expiry as seconds
     *
     * @return The remaining time until session expiration in seconds
     */
    public int getRemainingTimeSeconds() {
        return (int) remainingTimeSeconds;
    }

    /**
     * Returns the user model of this session
     *
     * @return The user model containing user information
     */
    public UserModel getUserModel() {
        return userModel;
    }

    /**
     * Returns the CSRF token bound to this session
     *
     * @return The CSRF token, or {@code null} if none is bound
     */
    public String getCsrfToken() {
        return csrfToken;
    }

    /**
     * Returns the id of the session (Not id of the user)
     *
     * @return The unique identifier for this session
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the id of the user
     *
     * @return The unique identifier of the user
     */
    public String getUserId() {

        return userModel.getId();
    }
}
