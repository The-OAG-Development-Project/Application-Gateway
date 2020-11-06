package ch.gianlucafrei.nellygateway.cookies;

import ch.gianlucafrei.nellygateway.utils.CookieUtils;
import ch.gianlucafrei.nellygateway.utils.JWEGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class SessionCookie {

    public static final String NAME = "session";
    public static final String SAMESITE = "lax";

    private long sessionExp;
    private String provider;
    private String subject;
    private String orginalToken;

    public SessionCookie() {
    }

    public static SessionCookie loadFromRequest(HttpServletRequest request, JWEGenerator jweGenerator) {

        Cookie cookie = CookieUtils.getCookieOrNull(NAME, request);

        if (cookie == null)
            return null;

        try {
            SessionCookie sessionCookie = jweGenerator.decryptObject(cookie.getValue(), SessionCookie.class);
            return sessionCookie;
        } catch (Exception e) {
            // TODO log
            return null;
        }
    }

    public Cookie getEncryptedHttpCookie(JWEGenerator jweGenerator, int maxAge) {

        String encryptedSessionCookie = jweGenerator.encryptObject(this);
        Cookie cookie = new Cookie(NAME, encryptedSessionCookie);
        cookie.setSecure(false); // TODO only for debugging via http only
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");

        return cookie;
    }

    public long getSessionExp() {
        return sessionExp;
    }

    public void setSessionExp(long sessionExp) {
        this.sessionExp = sessionExp;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOrginalToken() {
        return orginalToken;
    }

    public void setOrginalToken(String orginalToken) {
        this.orginalToken = orginalToken;
    }
}
