package ch.gianlucafrei.nellygateway.services.csrf;

import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component("csrf-double-submit-cookie-validation")
public class CsrfDoubleSubmitValidation implements CsrfProtectionValidation {

    public static final String NAME = "double-submit-cookie";
    public static final String CSRF_TOKEN_HEADER_NAME = "X-CSRF-TOKEN";
    public static final String CSRF_TOKEN_PARAMETER_NAME = "CSRFToken";


    @Override
    public boolean shouldBlockRequest(HttpServletRequest request) {

        Optional<Session> sessionOptional = (Optional<Session>) request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = (String) request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION_CSRF_TOKEN);
            String csrfValueFromDoubleSubmit = extractCsrfToken(request);

            if (csrfValueFromDoubleSubmit == null)
                return true;

            if (!csrfValueFromDoubleSubmit.equals(csrfValueFromSession))
                return true;
        }

        return false;
    }

    private String extractCsrfToken(HttpServletRequest request) {

        // Return from header if present
        String csrfTokenFromHeader = request.getHeader(CSRF_TOKEN_HEADER_NAME);
        if (csrfTokenFromHeader != null)
            return csrfTokenFromHeader;

        // Return token from parameter or null if not present
        String csrfFromParam = request.getParameter(CSRF_TOKEN_PARAMETER_NAME);
        return csrfFromParam;
    }
}
