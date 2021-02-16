package ch.gianlucafrei.nellygateway.session;

import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;

import java.time.Clock;
import java.util.Optional;

public class Session {
    private final long sessionExpSeconds;
    private final long remainingTimeSeconds;
    private final String provider;
    private final UserModel userModel;
    private final LoginCookie loginCookie;
    private final String id;

    private Session(long sessionExpSeconds, long remainingTimeSeconds, String provider, UserModel userModel, LoginCookie loginCookie, String id) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.provider = provider;
        this.userModel = userModel;
        this.loginCookie = loginCookie;
        this.id = id;
    }

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

    public String getProvider() {
        return provider;
    }

    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    public int getRemainingTimeSeconds() {
        return (int) remainingTimeSeconds;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public LoginCookie getLoginCookie() {
        return loginCookie;
    }

    public String getId() {
        return id;
    }
}
