package ch.gianlucafrei.nellygateway.cookies;

import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.utils.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class SessionCookie {

    public static final String NAME = "session";
    public static final String SAMESITE = "lax";

    private long sessionExp;
    private String provider;
    private String subject;
    private String orginalToken;

    public static SessionCookie loadFromRequest(HttpServletRequest request, CookieEncryptor encrypter) {

        Cookie cookie = CookieUtils.getCookieOrNull(NAME, request);

        if (cookie == null)
            return null;

        try {
            return encrypter.decryptObject(cookie.getValue(), SessionCookie.class);
        } catch (Exception e) {
            // TODO log
            return null;
        }
    }

    public Cookie getEncryptedHttpCookie(CookieEncryptor cookieEncryptor, int maxAge) {

        String encryptedSessionCookie = cookieEncryptor.encryptObject(this);
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
