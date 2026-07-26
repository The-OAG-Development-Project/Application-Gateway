package org.owasp.oag.hooks.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CookieWriter;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Session hook that creates and manages CSRF cookies.
 */
@Component
public class CsrfCookieCreationHook implements SessionHook {

    @Autowired
    private MainConfig config;

    @Autowired
    private CookieConverter cookieConverter;

    /**
     * Renews the session by copying the CSRF token from the old session.
     *
     * @param filterContext The filter context.
     * @param response      The HTTP response.
     */
    @Override
    public void renewSession(Map<String, Object> filterContext, HttpServletResponse response) {

        Session session = (Session) filterContext.get("old-session");
        filterContext.put("csrfToken", session.getLoginCookie().getCsrfToken());
    }

    /**
     * Gets the order of this hook.
     *
     * @return The order of this hook.
     */
    @Override
    public int order() {
        return 1;
    }

    /**
     * Creates a new CSRF cookie and adds it to the response.
     *
     * @param filterContext The filter context.
     * @param response      The HTTP response.
     */
    @Override
    public void createSession(Map<String, Object> filterContext, HttpServletResponse response) {

        var csrfToken = UUID.randomUUID().toString();
        var csrfCookie = new CsrfCookie(csrfToken);
        var sessionDuration = config.getSessionBehaviour().getSessionDuration();

        CookieWriter.addCookie(response, cookieConverter.convertCsrfCookie(csrfCookie, sessionDuration));
        filterContext.put("csrfToken", csrfToken);
    }

    /**
     * Destroys the CSRF cookie by setting its max-age to 0.
     *
     * @param filterContext The filter context.
     * @param request       The current request.
     * @param response      The HTTP response.
     */
    @Override
    public void destroySession(Map<String, Object> filterContext, HttpServletRequest request,
                              HttpServletResponse response) {

        // Override csrf cookie with new cookie that has max-age = 0
        var csrfToken = UUID.randomUUID().toString();
        var csrfCookie = new CsrfCookie(csrfToken);

        CookieWriter.addCookie(response, cookieConverter.convertCsrfCookie(csrfCookie, 0));
    }
}
