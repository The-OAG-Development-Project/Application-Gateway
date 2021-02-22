package org.owasp.oag.services.csrf;

import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Component("csrf-double-submit-cookie-with-body-validation")
public class CsrfDoubleSubmitInPostBody extends CsrfDoubleSubmitValidation {

    @Override
    public boolean needsRequestBody() {
        return true;
    }

    @Override
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromDoubleSubmit = extractCsrfToken(exchange);

            if (requestBody == null) {
                throw new AssertionError("request body is null");
            }

            if (requestBody.contains(csrfValueFromSession))
                return false;

            if (csrfValueFromDoubleSubmit == null)
                return true;

            return !csrfValueFromDoubleSubmit.equals(csrfValueFromSession);
        }

        return false;
    }
}