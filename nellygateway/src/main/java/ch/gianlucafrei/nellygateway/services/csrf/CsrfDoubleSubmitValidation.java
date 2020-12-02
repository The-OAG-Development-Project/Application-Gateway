package ch.gianlucafrei.nellygateway.services.csrf;

import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component("doubleSubmitCookie-validation")
public class CsrfDoubleSubmitValidation implements CsrfProtectionValidation {

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
        String csrfTokenFromHeader = request.getHeader("csrf");
        if (csrfTokenFromHeader != null)
            return csrfTokenFromHeader;

        // Return token from parameter or null if not present
        String csrfFromParam = request.getParameter("csrf");
        return csrfFromParam;
    }
}
