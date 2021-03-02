package org.owasp.oag.services.csrf;

import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Component("csrf-samesite-strict-cookie-validation")
public class CsrfSamesiteStrictValidation implements CsrfProtectionValidation {

    public static final String NAME = "samesite-strict-cookie";

    @Autowired
    CookieConverter cookieConverter;

    @Override
    public boolean needsRequestBody() {
        return false;
    }

    @Override
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromCookie = extractCsrfToken(exchange.getRequest());

            if (csrfValueFromCookie == null)
                return true;

            return !csrfValueFromCookie.equals(csrfValueFromSession);
        }

        return false;
    }

    private String extractCsrfToken(ServerHttpRequest request) {

        HttpCookie cookie = request.getCookies().getFirst(CsrfCookie.NAME);

        if (cookie == null)
            return null;

        CsrfCookie csrfCookie = cookieConverter.convertCsrfCookie(cookie);

        if (csrfCookie == null)
            return null;

        return csrfCookie.getCsrfToken();
    }
}
