package org.owasp.oag.services.csrf;

import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logInfo;

@Component("csrf-double-submit-cookie-validation")
public class CsrfDoubleSubmitValidation implements CsrfProtectionValidation {

    public static final String NAME = "double-submit-cookie";
    public static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-TOKEN";
    public static final String CSRF_TOKEN_PARAMETER_NAME = "CSRFToken";


    private static final Logger log = LoggerFactory.getLogger(CsrfDoubleSubmitValidation.class);

    @Override
    public boolean needsRequestBody() {
        return false;
    }

    @Override
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromDoubleSubmit = extractCsrfToken(exchange);

            if (csrfValueFromDoubleSubmit == null)
            {
                logInfo(log, exchange, "No csrf double submit value present in request");
                return true;
            }

            if(! csrfValueFromDoubleSubmit.equals(csrfValueFromSession)){

                logInfo(log, exchange, "Csrf value from double submit does not equal csrf token from cookie");
                return true;
            }
        }

        return false;
    }

    protected String extractCsrfToken(ServerWebExchange exchange) {

        var request = exchange.getRequest();

        // Return from header if present
        String csrfTokenFromHeader = request.getHeaders().getFirst(CSRF_TOKEN_HEADER_NAME);
        if (csrfTokenFromHeader != null)
            return csrfTokenFromHeader;

        // Return token from parameter or null if not present
        String csrfFromParam = request.getQueryParams().getFirst(CSRF_TOKEN_PARAMETER_NAME);
        return csrfFromParam;
    }
}
