package org.owasp.oag.services.csrf;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

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
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        return false;
    }
}
