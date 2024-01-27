package org.owasp.oag.services.csrf;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

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
