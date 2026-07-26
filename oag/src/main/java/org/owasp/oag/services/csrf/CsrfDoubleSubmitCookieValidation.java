package org.owasp.oag.services.csrf;

import jakarta.servlet.http.HttpServletRequest;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
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
     * @param request     The current request.
     * @param requestBody The request body (not used in this implementation).
     * @return true if the request should be blocked, false otherwise.
     */
    @Override
    public boolean shouldBlockRequest(HttpServletRequest request, String requestBody) {

        Optional<Session> sessionOptional = ExtractAuthenticationFilter.extractSessionFromRequest(request);

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = (String) request.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromDoubleSubmit = extractCsrfToken(request);

            if (csrfValueFromDoubleSubmit == null) {
                logInfo(log, request, "No csrf double submit value present in request");
                return true;
            }

            if (!csrfValueFromDoubleSubmit.equals(csrfValueFromSession)) {

                logInfo(log, request, "Csrf value from double submit does not equal csrf token from cookie");
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts the CSRF token from the request.
     *
     * @param request The current request.
     * @return The extracted CSRF token, or null if not found.
     */
    protected String extractCsrfToken(HttpServletRequest request) {

        // Return from header if present
        String csrfTokenFromHeader = request.getHeader(CSRF_TOKEN_HEADER_NAME);
        if (csrfTokenFromHeader != null)
            return csrfTokenFromHeader;

        // Return token from query parameter or null if not present
        return firstQueryParameter(request, CSRF_TOKEN_PARAMETER_NAME);
    }

    /**
     * Reads a query parameter from the request's query string.
     * <p>
     * The query string is parsed instead of using {@code HttpServletRequest#getParameter}, because the
     * servlet container consumes the request body of a form POST to populate its parameter map, which
     * would drain the body the gateway still has to forward downstream.
     *
     * @param request The current request
     * @param name    The name of the query parameter
     * @return The decoded value of the first parameter with that name, or null when absent
     */
    private static String firstQueryParameter(HttpServletRequest request, String name) {

        var queryString = request.getQueryString();

        if (queryString == null || queryString.isEmpty())
            return null;

        var value = UriComponentsBuilder.fromUriString("?" + queryString).build().getQueryParams().getFirst(name);
        return value == null ? null : UriUtils.decode(value, StandardCharsets.UTF_8);
    }
}
