package org.owasp.oag.hooks.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CookieWriter;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;

/**
 * Hook for creating, renewing, and destroying session cookies
 * Handles the lifecycle of session cookies including creation of new session IDs
 */
@Component
public class SessionCookieCreationHook implements SessionHook {

    @Autowired
    private CookieConverter cookieConverter;

    @Autowired
    private MainConfig config;

    @Autowired
    private GlobalClockSource globalClockSource;

    @Override
    public void renewSession(Map<String, Object> filterContext, HttpServletResponse response) {

        Session session = (Session) filterContext.get("old-session");

        filterContext.put("providerKey", session.getProvider());
        filterContext.put("userModel", session.getUserModel());

        createSession(filterContext, response); // Create a new session cookie which overwrites the old one
    }

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void createSession(Map<String, Object> filterContext, HttpServletResponse response) {

        String providerKey = (String) filterContext.get("providerKey");
        UserModel model = (UserModel) filterContext.get("userModel");

        int currentTimeSeconds = (int) (globalClockSource.getGlobalClock().millis() / 1000);
        int sessionDuration = config.getSessionBehaviour().getSessionDuration();
        int sessionExp = currentTimeSeconds + sessionDuration;
        var sessionId = createNewSessionId();
        LoginCookie loginCookie = new LoginCookie(sessionExp, providerKey, model, sessionId);

        // Bind csrf token to encrypted login cookie
        if (filterContext.containsKey("csrfToken")) {
            var csrfToken = (String) filterContext.get("csrfToken");
            loginCookie.setCsrfToken(csrfToken);
        }

        CookieWriter.addCookie(response, cookieConverter.convertLoginCookie(loginCookie, sessionDuration));
    }

    @Override
    public void destroySession(Map<String, Object> filterContext, HttpServletRequest request,
                              HttpServletResponse response) {

        // Override session cookie with new cookie that has max-age = 0
        CookieWriter.addCookie(response, cookieConverter.convertLoginCookie(null, 0));
    }

    private String createNewSessionId(){

        SecureRandom secureRandom = new SecureRandom();
        var id = secureRandom.nextLong();
        var idStr = Long.toHexString(id);
        return idStr;
    }
}
