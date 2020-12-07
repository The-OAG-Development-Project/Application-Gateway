package ch.gianlucafrei.nellygateway.filters.session;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
public class CsrfCookieCreationFilter implements NellySessionFilter {

    @Autowired
    NellyConfig config;

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void createSession(Map<String, Object> filterContext, HttpServletResponse response) {

        var csrfToken = UUID.randomUUID().toString();
        var cookie = new Cookie(CsrfCookie.NAME, new CsrfCookie(csrfToken).getCsrfToken());
        int sessionDuration = config.getSessionBehaviour().getSessionDuration();

        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(sessionDuration);
        cookie.setSecure(config.isHttpsHost());

        filterContext.put("csrfToken", csrfToken);
        CookieUtils.addSameSiteCookie(cookie, "Strict", response);
    }

    @Override
    public void destroySession(Map<String, Object> filterContext, HttpServletResponse response) {

        // Override csrf cookie with new cookie that has max-age = 0
        Cookie cookie = new Cookie(LoginCookie.NAME, "");
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(config.isHttpsHost());
        CookieUtils.addSameSiteCookie(cookie, "Strict", response);
    }
}
