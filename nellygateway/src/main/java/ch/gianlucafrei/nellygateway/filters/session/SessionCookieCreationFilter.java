package ch.gianlucafrei.nellygateway.filters.session;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;
import ch.gianlucafrei.nellygateway.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class SessionCookieCreationFilter implements NellySessionFilter {

    @Autowired
    private CookieEncryptor cookieEncryptor;

    @Autowired
    private NellyConfig config;

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void doFilter(Map<String, Object> filterContext, HttpServletResponse response) {

        String providerKey = (String) filterContext.get("providerKey");
        UserModel model = (UserModel) filterContext.get("userModel");

        int currentTimeSeconds = (int) (System.currentTimeMillis() / 1000);
        int sessionDuration = config.getSessionBehaviour().getSessionDuration();
        int sessionExp = currentTimeSeconds + sessionDuration;

        LoginCookie loginCookie = new LoginCookie(sessionExp, providerKey, model);

        // Bind csrf token to encrypted login cookie
        if (filterContext.containsKey("csrfToken")) {
            var csrfToken = (String) filterContext.get("csrfToken");
            loginCookie.setCsrfToken(csrfToken);
        }

        String encryptedLoginCookie = cookieEncryptor.encryptObject(loginCookie);

        Cookie cookie = new Cookie(LoginCookie.NAME, encryptedLoginCookie);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(sessionDuration);
        cookie.setSecure(config.isHttpsHost());
        CookieUtils.addSameSiteCookie(cookie, LoginCookie.SAMESITE, response);
    }
}
