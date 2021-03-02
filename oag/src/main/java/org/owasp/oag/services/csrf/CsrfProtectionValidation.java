package org.owasp.oag.services.csrf;

import org.springframework.web.server.ServerWebExchange;

public interface CsrfProtectionValidation {

    boolean needsRequestBody();

    boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody);
}
