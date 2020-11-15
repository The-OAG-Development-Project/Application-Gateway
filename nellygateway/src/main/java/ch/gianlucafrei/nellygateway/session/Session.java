package ch.gianlucafrei.nellygateway.session;

import ch.gianlucafrei.nellygateway.cookies.SessionCookie;

import java.util.Optional;

public class Session {

    private long sessionExpSeconds;
    private long remainingTimeSeconds;
    private String provider;
    private String subject;
    private String orginalToken;

    private Session(long sessionExpSeconds, long remainingTimeSeconds, String provider, String subject, String orginalToken) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.provider = provider;
        this.subject = subject;
        this.orginalToken = orginalToken;
    }

    public static Optional<Session> fromSessionCookie(SessionCookie cookie){

        if(cookie == null)
            return Optional.empty();

        long remainingTimeSeconds = cookie.getSessionExp() - (System.currentTimeMillis() / 1000);
        if(remainingTimeSeconds < 0)
            return Optional.empty();

        Session session = new Session(
                cookie.getSessionExp(),
                remainingTimeSeconds,
                cookie.getProvider(),
                cookie.getSubject(),
                cookie.getOrginalToken()
        );

        return Optional.of(session);
    }

    public String getProvider() {
        return provider;
    }

    public String getSubject() {
        return subject;
    }

    public String getOrginalToken() {
        return orginalToken;
    }

    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    public long getRemainingTimeSeconds() {
        return remainingTimeSeconds;
    }
}
