package org.owasp.oag.services.csrf;

import jakarta.servlet.http.HttpServletRequest;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates CSRF protection using the double-submit cookie method, including the request body.
 */
@Component
public class CsrfDoubleSubmitCookieWithBodyValidation extends CsrfDoubleSubmitCookieValidation {

    /**
     * The {@code csrfProtection} value handled by this validation.
     */
    public static final String TYPE = "doubleSubmitCookieWithBody";

    @Override
    public String type() {
        return TYPE;
    }

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
     * @param request     The current request.
     * @param requestBody The request body.
     * @return true if the request should be blocked, false otherwise.
     * @throws AssertionError if the request body is null.
     */
    @Override
    public boolean shouldBlockRequest(HttpServletRequest request, String requestBody) {

        Optional<Session> sessionOptional = Session.REQUEST_SESSION.get();

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = CsrfCookie.REQUEST_CSRF_TOKEN.get();
            String csrfValueFromDoubleSubmit = extractCsrfToken(request);

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
