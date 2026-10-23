package org.owasp.oag.services.csrf;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Implementation of CSRF protection validation that performs no validation.
 * This class always allows requests to pass through without any CSRF checks,
 * effectively disabling CSRF protection when used.
 * It should only be used in scenarios where CSRF protection is not required
 * or is handled by other means.
 */
@Component
public class CsrfNoneValidation implements CsrfProtectionValidation {

    @Override
    public boolean needsRequestBody() {
        return false;
    }

    @Override
    public boolean shouldBlockRequest(HttpServletRequest request, String requestBody) {

        return false;
    }
}
