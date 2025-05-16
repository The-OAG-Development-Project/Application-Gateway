package org.owasp.oag.services.csrf;

import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logInfo;

/**
 * Validates CSRF protection using the double-submit cookie method.
 */
@Component
public class CsrfDoubleSubmitCookieValidation implements CsrfProtectionValidation {

    /**
     * The name of the HTTP header used to submit the CSRF token.
     * This header is checked during CSRF validation to verify that the token matches the one in the session.
     */
    public static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-TOKEN";

    /**
     * The name of the query parameter used to submit the CSRF token.
     * This parameter is checked during CSRF validation if the token is not found in the header.
     */
    public static final String CSRF_TOKEN_PARAMETER_NAME = "CSRFToken";

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(CsrfDoubleSubmitCookieValidation.class);

    /**
     * Indicates whether this validation needs the request body.
     *
     * @return false, as this validation does not need the request body.
     */
    @Override
    public boolean needsRequestBody() {
        return false;
    }

    /**
     * Determines whether the request should be blocked based on CSRF validation.
     *
     * @param exchange    The server web exchange.
     * @param requestBody The request body (not used in this implementation).
     * @return true if the request should be blocked, false otherwise.
     */
    @Override
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional != null && sessionOptional.isPresent()) {
            String csrfValueFromSession = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromDoubleSubmit = extractCsrfToken(exchange);

            if (csrfValueFromDoubleSubmit == null) {
                logInfo(log, exchange, "No csrf double submit value present in request");
                return true;
            }

            if (!csrfValueFromDoubleSubmit.equals(csrfValueFromSession)) {

                logInfo(log, exchange, "Csrf value from double submit does not equal csrf token from cookie");
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts the CSRF token from the request.
     *
     * @param exchange The server web exchange.
     * @return The extracted CSRF token, or null if not found.
     */
    protected String extractCsrfToken(ServerWebExchange exchange) {

        var request = exchange.getRequest();

        // Return from header if present
        String csrfTokenFromHeader = request.getHeaders().getFirst(CSRF_TOKEN_HEADER_NAME);
        if (csrfTokenFromHeader != null)
            return csrfTokenFromHeader;

        // Return token from parameter or null if not present
        return request.getQueryParams().getFirst(CSRF_TOKEN_PARAMETER_NAME);
    }
}
