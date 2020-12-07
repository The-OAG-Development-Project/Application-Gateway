package ch.gianlucafrei.nellygateway.services.csrf;

import ch.gianlucafrei.nellygateway.cookies.CsrfCookie;
import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
import ch.gianlucafrei.nellygateway.utils.CookieUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component("csrf-samesite-strict-cookie-validation")
public class CsrfSamesiteStrictValidation implements CsrfProtectionValidation {

    @Override
    public boolean shouldBlockRequest(HttpServletRequest request) {

        Optional<Session> sessionOptional = (Optional<Session>) request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if (sessionOptional.isPresent()) {
            String csrfValueFromSession = (String) request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION_CSRF_TOKEN);
            String csrfValueFromCookie = extractCsrfToken(request);

            if (csrfValueFromCookie == null)
                return true;

            if (!csrfValueFromCookie.equals(csrfValueFromSession))
                return true;
        }

        return false;
    }

    private String extractCsrfToken(HttpServletRequest request) {

        var cookie = CookieUtils.getCookieOrNull(CsrfCookie.NAME, request);
        return cookie == null ? null : cookie.getValue();
    }
}
