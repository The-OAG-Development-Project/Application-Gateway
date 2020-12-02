package ch.gianlucafrei.nellygateway.filters.session;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
public class CsrfCookieCreationFilter implements NellySessionFilter {

    @Autowired
    NellyConfig nellyConfig;

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void doFilter(Map<String, Object> filterContext, HttpServletResponse response) {

        var csrfToken = UUID.randomUUID().toString();
        var cookie = new Cookie(CsrfCookie.NAME, new CsrfCookie(csrfToken).getCsrfToken());

        cookie.setHttpOnly(false);
        cookie.setSecure(nellyConfig.isHttpsHost());

        filterContext.put("csrfToken", csrfToken);

        response.addCookie(cookie);
    }
}
