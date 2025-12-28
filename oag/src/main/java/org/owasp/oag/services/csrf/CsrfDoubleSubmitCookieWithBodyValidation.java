package org.owasp.oag.services.csrf;

import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

/**
 * Validates CSRF protection using the double-submit cookie method, including the request body.
 */
@Component
public class CsrfDoubleSubmitCookieWithBodyValidation extends CsrfDoubleSubmitCookieValidation {

    /**
     * Indicates whether this validation needs the request body.
     *
     * @return true, as this validation needs the request body.
     */
    @Override
    public boolean needsRequestBody() {
        return true;
    }

    /**
     * Determines whether the request should be blocked based on CSRF validation, including the request body.
     *
     * @param exchange    The server web exchange.
     * @param requestBody The request body.
     * @return true if the request should be blocked, false otherwise.
     * @throws AssertionError if the request body is null.
     */
    @Override
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional != null && sessionOptional.isPresent()) {
            String csrfValueFromSession = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromDoubleSubmit = extractCsrfToken(exchange);

            if (requestBody == null) {
                throw new AssertionError("request body is null");
            }

            if (csrfValueFromSession != null && requestBody.contains(csrfValueFromSession))
                return false;

            if (csrfValueFromDoubleSubmit == null)
                return true;

            return !csrfValueFromDoubleSubmit.equals(csrfValueFromSession);
        }

        return false;
    }
}
