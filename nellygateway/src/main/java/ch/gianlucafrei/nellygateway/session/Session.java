package ch.gianlucafrei.nellygateway.session;

import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Session {

    private static final Logger log = LoggerFactory.getLogger(ExtractAuthenticationFilter.class);

    private final long sessionExpSeconds;
    private final long remainingTimeSeconds;
    private final String provider;
    private final UserModel userModel;

    private Session(long sessionExpSeconds, long remainingTimeSeconds, String provider, UserModel userModel) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.provider = provider;
        this.userModel = userModel;
    }

    public static Optional<Session> fromSessionCookie(LoginCookie cookie) {

        if (cookie == null)
            return Optional.empty();

        long remainingTimeSeconds = cookie.getSessionExpSeconds() - (System.currentTimeMillis() / 1000);
        if (remainingTimeSeconds < 0) {
            log.info("received expired session cookie");
            return Optional.empty();
        }


        Session session = new Session(
                cookie.getSessionExpSeconds(),
                remainingTimeSeconds,
                cookie.getProviderKey(),
                cookie.getUserModel()
        );

        return Optional.of(session);
    }

    public String getProvider() {
        return provider;
    }

    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    public long getRemainingTimeSeconds() {
        return remainingTimeSeconds;
    }

    public UserModel getUserModel() {
        return userModel;
    }
}
