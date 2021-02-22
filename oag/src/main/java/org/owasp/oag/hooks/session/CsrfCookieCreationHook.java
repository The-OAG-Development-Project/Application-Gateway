package org.owasp.oag.hooks.session;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.UUID;

@Component
public class CsrfCookieCreationHook implements SessionHook {

    @Autowired
    MainConfig config;

    @Autowired
    CookieConverter cookieConverter;

    @Override
    public void renewSession(Map<String, Object> filterContext, ServerHttpResponse response) {

        Session session = (Session) filterContext.get("old-session");
        filterContext.put("csrfToken", session.getLoginCookie().getCsrfToken());
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void createSession(Map<String, Object> filterContext, ServerHttpResponse response) {

        var csrfToken = UUID.randomUUID().toString();
        var csrfCookie = new CsrfCookie(csrfToken);
        var sessionDuration = config.getSessionBehaviour().getSessionDuration();

        response.addCookie(cookieConverter.convertCsrfCookie(csrfCookie, sessionDuration));
        filterContext.put("csrfToken", csrfToken);
    }

    @Override
    public void destroySession(Map<String, Object> filterContext, ServerWebExchange exchange) {

        // Override csrf cookie with new cookie that has max-age = 0
        var csrfToken = UUID.randomUUID().toString();
        var csrfCookie = new CsrfCookie(csrfToken);

        exchange.getResponse().addCookie(cookieConverter.convertCsrfCookie(csrfCookie, 0));
    }
}
