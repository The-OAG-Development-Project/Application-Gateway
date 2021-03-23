package org.owasp.oag.session;

import org.owasp.oag.cookies.LoginCookie;

import java.time.Clock;
import java.util.Optional;

/**
 * This class is the main representation of a user session within OAG.
 * The user session information is serialized in encrypted form in the session cookie and extracted
 * when a request is processed.
 */
public class Session {
    private final long sessionExpSeconds;
    private final long remainingTimeSeconds;
    private final String provider;
    private final UserModel userModel;
    private final LoginCookie loginCookie;
    private final String id;

    /**
     * Creates a new Session object with all required values
     *
     * @param sessionExpSeconds
     * @param remainingTimeSeconds
     * @param provider
     * @param userModel
     * @param loginCookie
     * @param id
     */
    public Session(long sessionExpSeconds, long remainingTimeSeconds, String provider, UserModel userModel, LoginCookie loginCookie, String id) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.provider = provider;
        this.userModel = userModel;
        this.loginCookie = loginCookie;
        this.id = id;
    }

    /**
     * Converts a decrypted login cookie into a user session object.
     * Returns an empty optional is the session is expired.
     *
     * @param cookie
     * @param clock
     * @return
     */
    public static Optional<Session> fromSessionCookie(LoginCookie cookie, Clock clock) {

        if (cookie == null)
            return Optional.empty();

        long remainingTimeSeconds = cookie.getSessionExpSeconds() - (clock.millis() / 1000);
        if (remainingTimeSeconds < 0) {
            return Optional.empty();
        }


        Session session = new Session(
                cookie.getSessionExpSeconds(),
                remainingTimeSeconds,
                cookie.getProviderKey(),
                cookie.getUserModel(),
                cookie,
                cookie.getId()
        );

        return Optional.of(session);
    }

    /**
     * Returns the name of the used login provider
     *
     * @return
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the end of the session validity as unix timestamp
     *
     * @return
     */
    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    /**
     * Returns the duration until session expiry as seconds
     *
     * @return
     */
    public int getRemainingTimeSeconds() {
        return (int) remainingTimeSeconds;
    }

    /**
     * Returns the user model of this session
     *
     * @return
     */
    public UserModel getUserModel() {
        return userModel;
    }

    /**
     * Returns the login cookie that represents this session
     *
     * @return
     */
    public LoginCookie getLoginCookie() {
        return loginCookie;
    }

    /**
     * Returns the is of the session (Not id of the user)
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the id of the user
     *
     * @return
     */
    public String getUserId() {

        return userModel.getId();
    }
}
